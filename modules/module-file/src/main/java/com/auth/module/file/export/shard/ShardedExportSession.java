package com.auth.module.file.export.shard;

import com.auth.module.file.export.writer.TempFileSupport;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 按分片阈值拆分落盘的导出会话
 *
 * <p>
 * 写入完成后调用 {@link #finish()} 获取所有分片路径；任何失败路径均应调用 {@link #abortQuietly()} 清理临时文件
 *
 * @param <R> 导出行类型
 * @author Bunny
 */
public final class ShardedExportSession<R> {

	private final ChunkFileSessionFactory<R> sessionFactory;

	private final int shardRows;

	private final List<Path> completedPaths = new ArrayList<>();

	private ChunkFileSession<R> currentSession;

	private int currentShardWritten;

	private State state = State.WRITING;

	/**
	 * @param sessionFactory 单文件会话工厂
	 * @param shardRows 分片阈值（{@code <=0} 表示不分片）
	 */
	public ShardedExportSession(ChunkFileSessionFactory<R> sessionFactory, int shardRows) {
		this.sessionFactory = Objects.requireNonNull(sessionFactory, "sessionFactory must not be null");
		this.shardRows = shardRows;
	}

	/**
	 * 写入行流；跨分片边界时自动切开新文件
	 * @param rows 导出行
	 * @throws IOException 写入失败
	 */
	public void writeRows(List<R> rows) throws IOException {
		assertWriting();
		if (rows == null || rows.isEmpty()) {
			return;
		}
		if (shardRows <= 0) {
			writeWithoutSharding(rows);
			return;
		}
		writeWithSharding(rows);
	}

	/**
	 * 完成所有分片并返回路径列表（至少 1 个）
	 * @return 分片临时文件路径
	 * @throws IOException 完成失败
	 */
	public List<Path> finish() throws IOException {
		assertWriting();
		try {
			if (currentSession != null) {
				completeCurrentShard();
			}
			else if (completedPaths.isEmpty()) {
				// 0 行场景：仍需产出一个空文件，保持"至少一个分片"的契约
				ensureCurrentSession();
				completeCurrentShard();
			}
			state = State.FINISHED;
			return List.copyOf(completedPaths);
		}
		catch (IOException | RuntimeException exception) {
			abortQuietly();
			throw exception;
		}
	}

	/**
	 * 静默中止并清理已产生的临时文件；幂等，可在任何状态下安全调用
	 */
	public void abortQuietly() {
		if (state == State.FINISHED) {
			return;
		}
		state = State.ABORTED;
		closeCurrentSessionQuietly();
		deleteCompletedPathsQuietly();
	}

	private void writeWithoutSharding(List<R> rows) throws IOException {
		ensureCurrentSession();
		currentSession.writeChunk(rows);
		currentShardWritten += rows.size();
	}

	// ── 写入内部逻辑 ──────────────────────────────────────────────────────

	private void writeWithSharding(List<R> rows) throws IOException {
		int offset = 0;
		while (offset < rows.size()) {
			ensureCurrentSession();
			int capacity = shardRows - currentShardWritten;
			int end = Math.min(offset + capacity, rows.size());
			currentSession.writeChunk(rows.subList(offset, end));
			currentShardWritten += end - offset;
			offset = end;
			if (currentShardWritten >= shardRows && offset < rows.size()) {
				completeCurrentShard();
			}
		}
	}

	private void ensureCurrentSession() throws IOException {
		if (currentSession != null) {
			return;
		}
		currentSession = sessionFactory.open();
		currentShardWritten = 0;
	}

	private void completeCurrentShard() throws IOException {
		completedPaths.add(currentSession.complete());
		currentSession = null;
		currentShardWritten = 0;
	}

	private void assertWriting() {
		if (state == State.FINISHED) {
			throw new IllegalStateException("Export session already finished");
		}
		if (state == State.ABORTED) {
			throw new IllegalStateException("Export session already aborted");
		}
	}

	// ── 状态守卫与清理 ───────────────────────────────────────────────────

	private void closeCurrentSessionQuietly() {
		if (currentSession == null) {
			return;
		}
		try {
			currentSession.close();
		}
		catch (IOException ignored) {
			// best-effort cleanup
		}
		finally {
			currentSession = null;
		}
	}

	private void deleteCompletedPathsQuietly() {
		for (Path path : completedPaths) {
			TempFileSupport.deleteQuietly(path);
		}
		completedPaths.clear();
	}

	private enum State {

		WRITING, FINISHED, ABORTED

	}

}
