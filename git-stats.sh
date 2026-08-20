#!/bin/bash

# ==============================================================================
# Git 项目数据统计与分析脚本
# ==============================================================================

# 确保在 Git 仓库中运行
if ! git rev-parse --is-inside-work-tree > /dev/null 2>&1; then
    echo -e "\e[31m[错误] 当前目录或其父目录不是一个 Git 仓库。\e[0m"
    exit 1
fi

# 获取当前分支名称
CURRENT_BRANCH=$(git symbolic-ref --short HEAD 2>/dev/null || echo "HEAD (分离头指针)")

echo -e "\e[35m==================================================================\e[0m"
echo -e "\e[36m   Git 项目统计报告 - 分支: $CURRENT_BRANCH \e[0m"
echo -e "\e[35m==================================================================\e[0m"

# 1. 查看当前分支有几个未推送的 commit
echo -e "\n\e[32m[1] 未推送至远程的本地 Commit 数量:\e[0m"
if git rev-parse --verify @{u} > /dev/null 2>&1; then
    UNPUSHED_COUNT=$(git rev-list --count @{u}..HEAD)
    echo -e "    -> 本地领先远程追踪分支: \e[1;33m$UNPUSHED_COUNT\e[0m 个 commit"
else
    echo -e "    -> \e[33m未检测到远程追踪分支\e[0m (当前分支可能尚未推送到远程服务器)"
fi

# 2. 查看当前项目修改了多少代码量
echo -e "\n\e[32m[2] 当前工作区未提交的代码修改量:\e[0m"
DIFF_STAT=$(git diff HEAD --stat 2>/dev/null)
if [ -z "$DIFF_STAT" ]; then
    echo "    -> 当前工作区很干净，没有任何代码修改。"
else
    echo "$DIFF_STAT" | sed 's/^/    /'
fi

# 3. 查看当前项目体积多大（不含忽略文件）
echo -e "\n\e[32m[3] Git 追踪的代码文件总体积:\e[0m"
PROJECT_SIZE=$(git ls-files -z | xargs -0 du -ch 2>/dev/null | tail -n 1 | awk '{print $1}')
if [ -n "$PROJECT_SIZE" ]; then
    echo -e "    -> 纯代码文件总体积: \e[1;33m$PROJECT_SIZE\e[0m"
else
    echo "    -> 无法计算体积（可能是一个空仓库）"
fi

# 4. 查看当前项目总的代码量
echo -e "\n\e[32m[4] 当前项目总代码行数统计:\e[0m"
if command -v cloc >/dev/null 2>&1; then
    echo "    -> 已检测到 cloc 工具，正在进行精确代码统计..."
    cloc $(git ls-files) 2>/dev/null | sed 's/^/    /'
else
    echo -e "    -> \e[33m未安装 cloc 工具\e[0m，使用基础命令进行粗略统计 (包含空行和注释):"
    TOTAL_LINES=$(git ls-files | xargs wc -l 2>/dev/null | tail -n 1 | awk '{print $1}')
    if [ -n "$TOTAL_LINES" ]; then
        echo -e "       总行数 (粗略): \e[1;33m$TOTAL_LINES\e[0m 行"
    else
        echo "       无法统计行数或项目为空。"
    fi
fi

# 5. 查看当前项目一共 commit 几次
echo -e "\n\e[32m[5] 项目历史 Commit 总数:\e[0m"
TOTAL_COMMITS=$(git rev-list --count HEAD 2>/dev/null)
ALL_BRANCH_COMMITS=$(git rev-list --all --count 2>/dev/null)
echo -e "    -> 当前分支总 Commit 次数: \e[1;33m$TOTAL_COMMITS\e[0m 次"
echo -e "    -> 所有分支总 Commit 次数: \e[1;33m$ALL_BRANCH_COMMITS\e[0m 次"

# 6. 查看当前项目平均每天、每周 commit 几次
echo -e "\n\e[32m[6] Commit 频率与周期统计:\e[0m"
if [ "$TOTAL_COMMITS" -eq 0 ] 2>/dev/null; then
    echo "    -> 当前分支暂无提交记录，无法计算频率。"
else
    git log --format='%ad' --date=short 2>/dev/null | awk '
    {count[$1]++}
    END {
        for (date in count) { sum+=count[date]; days++ }
        if (days > 0) printf "    -> 【每天】活跃总天数: %d 天 | 平均每天提交: \033[1;33m%.2f\033[0m 次\n", days, sum/days
    }'

    git log --format='%ad' --date=format:'%Y-%U' 2>/dev/null | awk '
    {count[$1]++}
    END {
        for (week in count) { sum+=count[week]; weeks++ }
        if (weeks > 0) printf "    -> 【每周】跨越总周数: %d 周 | 平均每周提交: \033[1;33m%.2f\033[0m 次\n", weeks, sum/weeks
    }'

    git log --format='%ad' --date=format:'%Y-%m' 2>/dev/null | awk '
    {count[$1]++}
    END {
        for (month in count) { sum+=count[month]; months++ }
        if (months > 0) printf "    -> 【每月】跨越总月数: %d 月 | 平均每月提交: \033[1;33m%.2f\033[0m 次\n", months, sum/months
    }'
fi

# 7. 贡献者提交次数排行（新增部分）
echo -e "\n\e[32m[7] 贡献者提交次数排行 (Top Authors):\e[0m"
if [ "$TOTAL_COMMITS" -eq 0 ] 2>/dev/null; then
    echo "    -> 暂无提交数据。"
else
    # 打印表头，并使用 sed 给输出结果前加 4 个空格缩进保持对齐
    echo -e "       次数 作者"
    echo -e "       ---- ----"
    git log --format='%aN' 2>/dev/null | sort | uniq -c | sort -rn | sed 's/^/    /'
fi

echo -e "\n\e[35m==================================================================\e[0m"