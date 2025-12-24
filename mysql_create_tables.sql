-- Todo 表
CREATE TABLE `todo` (
  `id` VARCHAR(36) NOT NULL COMMENT '待办事项ID，UUID自动生成',
  `text` VARCHAR(255) NOT NULL COMMENT '待办事项文本内容',
  `checked` INT DEFAULT 0 COMMENT '待办事项完成状态，0为未完成，1为已完成',
  `list_id` VARCHAR(20) NOT NULL COMMENT '待办事项所属日期列表ID，格式如：20251217',
  `description` TEXT DEFAULT '' COMMENT '待办事项描述',
  `sub_todos` JSON COMMENT '待办事项的子任务列表',
  `priority` INT DEFAULT 0 COMMENT '待办事项优先级，数值越大优先级越高',
  `tags` JSON COMMENT '待办事项标签列表',
  `time` JSON COMMENT '待办事项的时间安排',
  `alarm` TINYINT(1) DEFAULT 0 COMMENT '待办事项是否设置闹钟提醒，0为不需要，1为需要',
  `repeating_event_id` VARCHAR(36) COMMENT '关联的重复事件ID',
  `task_id` VARCHAR(36) COMMENT '待办事项所属任务ID',
  `milestone_id` VARCHAR(36) COMMENT '待办事项所属里程碑ID',
  `color` VARCHAR(20) COMMENT '待办事项颜色标记',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '待办事项创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '待办事项最后更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_list_id` (`list_id`),
  KEY `idx_repeating_event_id` (`repeating_event_id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_milestone_id` (`milestone_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='待办事项表';

-- Task 表
CREATE TABLE `task` (
  `id` VARCHAR(36) NOT NULL COMMENT '任务ID，UUID自动生成',
  `title` VARCHAR(255) NOT NULL COMMENT '任务标题',
  `description` TEXT COMMENT '任务描述',
  `start_date` DATE COMMENT '任务开始日期',
  `end_date` DATE COMMENT '任务结束日期',
  `category` VARCHAR(50) COMMENT '任务分类',
  `priority` INT DEFAULT 0 COMMENT '任务优先级，数值越大优先级越高',
  `completed` INT DEFAULT 0 COMMENT '任务完成状态，0为未完成，1为已完成',
  `color` VARCHAR(20) COMMENT '任务颜色标记',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '任务创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '任务最后更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务表';

-- Milestone 表
CREATE TABLE `milestone` (
  `id` VARCHAR(36) NOT NULL COMMENT '里程碑ID，UUID自动生成',
  `title` VARCHAR(255) NOT NULL COMMENT '里程碑标题',
  `description` TEXT COMMENT '里程碑描述',
  `start_date` DATE COMMENT '里程碑开始日期',
  `end_date` DATE COMMENT '里程碑结束日期',
  `completed` TINYINT(1) DEFAULT FALSE COMMENT '里程碑完成状态',
  `task_id` VARCHAR(36) NOT NULL COMMENT '里程碑所属任务ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '里程碑创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '里程碑最后更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='里程碑表';

-- RepeatingEvent 表
CREATE TABLE `repeating_event` (
  `id` VARCHAR(36) NOT NULL COMMENT '重复事件ID，唯一标识符',
  `start_date` DATE COMMENT '重复事件的开始日期',
  `repeating_rule` VARCHAR(100) COMMENT '重复规则，定义事件的重复模式',
  `type` VARCHAR(50) COMMENT '重复事件的类型',
  `occurrences_type` VARCHAR(50) COMMENT '重复发生的类型，与重复规则相关',
  `todo_id` VARCHAR(36) NOT NULL COMMENT '重复事件关联的待办事项ID',
  `end_date` DATE COMMENT '重复事件的结束日期，超过此日期后事件不再重复',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '重复事件创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '重复事件最后更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_todo_id` (`todo_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='重复事件表';

-- 添加外键约束
ALTER TABLE `todo` ADD CONSTRAINT `fk_todo_repeating_event` FOREIGN KEY (`repeating_event_id`) REFERENCES `repeating_event` (`id`) ON DELETE SET NULL;
ALTER TABLE `todo` ADD CONSTRAINT `fk_todo_task` FOREIGN KEY (`task_id`) REFERENCES `task` (`id`) ON DELETE SET NULL;
ALTER TABLE `todo` ADD CONSTRAINT `fk_todo_milestone` FOREIGN KEY (`milestone_id`) REFERENCES `milestone` (`id`) ON DELETE SET NULL;
ALTER TABLE `milestone` ADD CONSTRAINT `fk_milestone_task` FOREIGN KEY (`task_id`) REFERENCES `task` (`id`) ON DELETE CASCADE;
ALTER TABLE `repeating_event` ADD CONSTRAINT `fk_repeating_event_todo` FOREIGN KEY (`todo_id`) REFERENCES `todo` (`id`) ON DELETE CASCADE;
AlTER TABLE `todo` ADD COLUMN `color` VARCHAR(20) COMMENT '待办事项颜色标记';