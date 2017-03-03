CREATE TABLE if not exists `data_source` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `sourceName` varchar(120) NOT NULL COMMENT '数据源名称',
  `ip` varchar(200) NOT NULL COMMENT 'IP',
  `port` int(6) NOT NULL COMMENT 'port',
  `userName` varchar(120) NOT NULL DEFAULT '' COMMENT '用户名',
  `password` varchar(120) NOT NULL DEFAULT '' COMMENT '密码',
  `dbType` int(2) NOT NULL DEFAULT 1 COMMENT '类型，1为Mysql 2为mongodb',
  `remarks` varchar(255) NOT NULL DEFAULT '' COMMENT '描述',
  PRIMARY KEY (`id`)
) DEFAULT CHARSET=utf8 COMMENT='数据源表';

CREATE TABLE if not exists `job_list` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `jobName` varchar(120) NOT NULL COMMENT '任务名称',
  `refSrcDbSourceId` int(10) NOT NULL COMMENT '所属数据源ID',
  `srcBase` varchar(120) NOT NULL COMMENT '来源库',
  `srcTable` varchar(200) NOT NULL COMMENT '来源表',
  `refDstDbSourceId` int(10) NOT NULL COMMENT '所属目标数据源ID',
  `dstBase` varchar(120) NOT NULL COMMENT '目标库',
  `dstTable` varchar(200) NOT NULL COMMENT '目标表',
  `incrementColumn` varchar(120)  COMMENT '自增列',
  `incrementColumnVal` varchar(120)  COMMENT '自增列值',
  `jobRule` text COMMENT 'Drools规则',
  `remarks` varchar(255) NOT NULL DEFAULT '' COMMENT '描述',
  PRIMARY KEY (`id`)
) DEFAULT CHARSET=utf8 COMMENT='任务表';

CREATE TABLE if not exists `schedule_info` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `scheduleName` varchar(120) NOT NULL COMMENT '调度计划名称',
  `refJobId` int(10) NOT NULL COMMENT '所属任务ID',
  `scheduleType` int(2) NOT NULL DEFAULT 1 COMMENT '调度类型，1为一次性，2为自定义计划',
  `schedule` varchar(120) NOT NULL COMMENT '调度计划',
  `status` int(2) NOT NULL DEFAULT 0 COMMENT '0表示初始化，1表示运行中，2表示成功，3,表示失败',
  `remarks` varchar(255) NOT NULL DEFAULT '' COMMENT '描述',
  PRIMARY KEY (`id`)
) DEFAULT CHARSET=utf8 COMMENT='调度表';

CREATE TABLE if not exists `execute_list` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `refScheduleId` int(10) NOT NULL COMMENT '所属调度ID',
  `executeLog` text NOT NULL COMMENT '执行内容',
  `status` int(2) NOT NULL DEFAULT 0 COMMENT '0表示初始化，1表示运行中，2表示成功，3,表示失败',
  `startTime` varchar(120) COMMENT '开始时间',
  `endTime` varchar(120) COMMENT '结束时间',
  `remarks` varchar(255) NOT NULL DEFAULT '' COMMENT '描述',
  PRIMARY KEY (`id`)
) DEFAULT CHARSET=utf8 COMMENT='调度执行表';