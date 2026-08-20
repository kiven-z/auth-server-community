package com.auth.service.system.schedule.support.quartz;

import com.auth.service.system.schedule.model.constants.SysJobQuartzDataKeys;
import com.auth.service.system.schedule.model.entity.JobEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link SysJobQuartzJobFactory} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("SysJobQuartzJobFactory JobDetail 构建")
class SysJobQuartzJobFactoryTest {

	private final SysJobQuartzJobFactory factory = new SysJobQuartzJobFactory();

	@Test
	@DisplayName("build：应将 job_params 原文与展开键同时写入 JobDataMap")
	void build_putsRawAndFlattenedJobParams() {
		JobEntity job = new JobEntity();
		job.setId(10L);
		job.setJobName("http-demo");
		job.setJobGroup("DEFAULT");
		job.setTaskType("CUSTOM_CLASS");
		job.setJobClass("com.example.HttpGetInvokeJob");
		job.setJobParams("{\"url\":\"https://example.com\",\"timeout\":3000}");
		job.setConcurrent(true);

		JobDetail jobDetail = factory.build(job);
		JobDataMap map = jobDetail.getJobDataMap();

		assertThat(map.getString(SysJobQuartzDataKeys.JOB_PARAMS))
			.isEqualTo("{\"url\":\"https://example.com\",\"timeout\":3000}");
		assertThat(map.getString("url")).isEqualTo("https://example.com");
		assertThat(map).containsEntry("timeout", 3000);
	}

}
