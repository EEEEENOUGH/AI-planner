create table t_role
(
    id          bigint unsigned auto_increment comment '主键'
        primary key,
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_deleted  tinyint  default 0                 not null comment '逻辑删除标识',
    role_name   varchar(50)                        not null comment '角色名称',
    role_type   tinyint  default 1                 not null comment '角色类型：1普通用户，2管理员',
    role_status tinyint  default 1                 not null comment '角色状态：0停用，1启用',
    constraint uk_role_name
        unique (role_name)
)
    comment '系统角色表' collate = utf8mb4_general_ci;

create table t_user_info
(
    id              bigint unsigned auto_increment comment '主键'
        primary key,
    create_time     datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time     datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_deleted      tinyint     default 0                 not null comment '逻辑删除标识',
    login_name      varchar(50)                           not null comment '登录账号',
    password_hash   varchar(255)                          not null comment '密码哈希',
    nickname        varchar(50) default ''                not null comment '昵称',
    phone           varchar(20)                           null comment '手机号',
    email           varchar(100)                          null comment '邮箱',
    avatar_url      varchar(255)                          null comment '头像地址',
    gender          tinyint     default 0                 not null comment '性别：0未知，1男，2女',
    user_status     tinyint     default 1                 not null comment '用户状态：0禁用，1启用',
    last_login_time datetime                              null comment '最后登录时间',
    create_ip       varchar(45)                           null comment '创建来源 IP',
    constraint uk_login_name
        unique (login_name)
)
    comment '用户基础信息表' collate = utf8mb4_general_ci;

create table t_exception_log
(
    id                bigint unsigned auto_increment comment '主键'
        primary key,
    create_time       datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time       datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_deleted        tinyint     default 0                 not null comment '逻辑删除标识',
    user_id           bigint unsigned                       null comment '异常关联用户 id（可为空）',
    exception_level   tinyint     default 2                 not null comment '异常级别：0信息，1警告，2错误',
    exception_type    varchar(255)                          not null comment '异常类型（类名）',
    exception_message varchar(500)                          null comment '异常消息',
    stack_trace       longtext                              null comment '堆栈信息（可截断策略在代码层实现）',
    request_method    varchar(10)                           null comment '请求方法',
    request_path      varchar(255)                          null comment '请求路径',
    handled_status    tinyint     default 0                 not null comment '是否已处理：0未处理，1已处理',
    log_source        varchar(50) default 'system'          not null comment '日志来源：system/web/api/worker 等',
    constraint fk_exception_log_user_id
        foreign key (user_id) references t_user_info (id)
            on update cascade
)
    comment '系统异常日志记录表' collate = utf8mb4_general_ci;

create index idx_exception_level
    on t_exception_log (exception_level);

create index idx_exception_type
    on t_exception_log (exception_type);

create index idx_request_path
    on t_exception_log (request_path);

create index idx_user_id
    on t_exception_log (user_id);

create table t_operation_log
(
    id               bigint unsigned auto_increment comment '主键'
        primary key,
    create_time      datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time      datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_deleted       tinyint     default 0                 not null comment '逻辑删除标识',
    user_id          bigint unsigned                       null comment '操作用户 id（系统任务可为空）',
    operation_type   tinyint     default 0                 not null comment '操作类型：0其他，1登录，2计划生成/调整，3任务完成/打卡，4权限管理',
    request_method   varchar(10) default 'GET'             not null comment '请求方法',
    request_path     varchar(255)                          not null comment '请求路径',
    ip_address       varchar(45)                           null comment '访问 IP',
    user_agent       varchar(512)                          null comment '用户代理',
    operation_detail longtext                              null comment '操作详情（JSON/文本）',
    constraint fk_operation_log_user_id
        foreign key (user_id) references t_user_info (id)
            on update cascade
)
    comment '系统操作日志表' collate = utf8mb4_general_ci;

create index idx_operation_type
    on t_operation_log (operation_type);

create index idx_request_path
    on t_operation_log (request_path);

create index idx_user_id
    on t_operation_log (user_id);

create table t_user_exam_archive
(
    id                   bigint unsigned auto_increment comment '主键'
        primary key,
    create_time          datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time          datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_deleted           tinyint  default 0                 not null comment '逻辑删除标识',
    user_id              bigint unsigned                    not null comment '用户 id',
    archive_name         varchar(100)                       not null comment '备考档案名称（用户自定义，如：2026考研-冲刺）',
    target_institution   varchar(100)                       not null comment '目标院校',
    target_major         varchar(100)                       not null comment '目标专业',
    daily_study_duration int      default 6                 not null comment '每日可复习时长',
    exam_subjects        varchar(255)                       not null comment '考试科目',
    exam_date            date                               not null comment '预计考试年份',
    archive_status       tinyint  default 1                 not null comment '档案状态：0归档/不可用，1启用',
    subject_mastery      varchar(255)                       not null comment '科目掌握情况',
    constraint uk_user_id_archive_name
        unique (user_id, archive_name),
    constraint fk_exam_archive_user_id
        foreign key (user_id) references t_user_info (id)
            on update cascade
)
    comment '用户考研备考档案表' collate = utf8mb4_general_ci;

create table t_ai_qa_history
(
    id              bigint unsigned auto_increment comment '主键'
        primary key,
    create_time     datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time     datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_deleted      tinyint      default 0                 not null comment '逻辑删除标识',
    user_id         bigint unsigned                        not null comment '用户 id',
    exam_archive_id bigint unsigned                        null comment '备考档案 id',
    question_title  varchar(200) default ''                not null comment '问题标题',
    question_text   longtext                               not null comment '用户问题内容',
    prompt_text     longtext                               null comment 'Prompt 原文',
    ai_answer_text  longtext                               not null comment 'AI 答案内容',
    model_name      varchar(100) default 'deepseek'        not null comment '模型名称',
    answer_status   tinyint      default 1                 not null comment '答疑状态：0失败，1成功',
    token_usage     int          default 0                 not null comment 'Token 使用量（估算/记录）',
    constraint fk_ai_qa_exam_archive_id
        foreign key (exam_archive_id) references t_user_exam_archive (id)
            on update cascade,
    constraint fk_ai_qa_user_id
        foreign key (user_id) references t_user_info (id)
            on update cascade
)
    comment 'AI答疑历史表' collate = utf8mb4_general_ci;

create index idx_answer_status
    on t_ai_qa_history (answer_status);

create index idx_exam_archive_id
    on t_ai_qa_history (exam_archive_id);

create index idx_user_id
    on t_ai_qa_history (user_id);

create table t_ai_synthesis_history
(
    id               bigint unsigned auto_increment comment '主键'
        primary key,
    create_time      datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time      datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_deleted       tinyint      default 0                 not null comment '逻辑删除标识',
    user_id          bigint unsigned                        not null comment '用户 id',
    exam_archive_id  bigint unsigned                        not null comment '备考档案 id',
    synthesis_title  varchar(200) default ''                not null comment '梳理标题',
    prompt_text      longtext                               not null comment 'Prompt 原文',
    ai_response_text longtext                               not null comment 'AI 返回内容',
    model_name       varchar(100) default 'deepseek'        not null comment '模型名称',
    synthesis_status tinyint      default 1                 not null comment '梳理状态：0失败，1成功',
    token_usage      int          default 0                 not null comment 'Token 使用量（估算/记录）',
    constraint fk_ai_synthesis_exam_archive_id
        foreign key (exam_archive_id) references t_user_exam_archive (id)
            on update cascade,
    constraint fk_ai_synthesis_user_id
        foreign key (user_id) references t_user_info (id)
            on update cascade
)
    comment 'AI知识点梳理历史表' collate = utf8mb4_general_ci;

create index idx_exam_archive_id
    on t_ai_synthesis_history (exam_archive_id);

create index idx_synthesis_status
    on t_ai_synthesis_history (synthesis_status);

create index idx_user_id
    on t_ai_synthesis_history (user_id);

create table t_study_plan_total
(
    id                   bigint unsigned auto_increment comment '主键'
        primary key,
    create_time          datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time          datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_deleted           tinyint  default 0                 not null comment '逻辑删除标识',
    user_id              bigint unsigned                    not null comment '用户 id',
    exam_archive_id      bigint unsigned                    not null comment '备考档案 id',
    plan_name            varchar(100)                       not null comment '总计划名称（版本/名称）',
    overall_start_date   date                               not null comment '总计划开始日期',
    overall_end_date     date                               not null comment '总计划结束日期',
    daily_target_minutes int      default 60                not null comment '日目标时长（分钟）',
    overall_goal         longtext                           not null comment '总体目标说明',
    create_source        tinyint  default 0                 not null comment '创建来源：0 AI生成/调整，1 人工创建/调整',
    modify_source        tinyint  default 0                 not null comment '修改来源：0 AI生成/调整，1 人工创建/调整',
    plan_status          tinyint  default 1                 not null comment '计划状态：0停用，1启用',
    constraint uk_user_id_exam_archive_id_plan_name
        unique (user_id, exam_archive_id, plan_name),
    constraint fk_plan_total_exam_archive_id
        foreign key (exam_archive_id) references t_user_exam_archive (id)
            on update cascade,
    constraint fk_plan_total_user_id
        foreign key (user_id) references t_user_info (id)
            on update cascade
)
    comment '复习总计划表' collate = utf8mb4_general_ci;

create table t_study_checkin
(
    id                      bigint unsigned auto_increment comment '主键'
        primary key,
    create_time             datetime      default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time             datetime      default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_deleted              tinyint       default 0                 not null comment '逻辑删除标识',
    user_id                 bigint unsigned                         not null comment '用户 id',
    total_plan_id           bigint unsigned                         not null comment '总计划 id',
    checkin_date            date                                    not null comment '打卡日期',
    checkin_status          tinyint       default 0                 not null comment '打卡状态：0未打卡，1已打卡',
    actual_duration_minutes int           default 0                 not null comment '实际复习时长（分钟）',
    finished_task_count     int           default 0                 not null comment '已完成任务数',
    total_task_count        int           default 0                 not null comment '任务总数',
    task_completion_rate    decimal(5, 2) default 0.00              not null comment '任务完成率（%）',
    remind_sent_status      tinyint       default 0                 not null comment '提醒发送状态：0未发送，1已发送',
    checkin_note            varchar(255)                            null comment '打卡备注/感想',
    constraint uk_user_id_total_plan_id_checkin_date
        unique (user_id, total_plan_id, checkin_date),
    constraint fk_study_checkin_total_plan_id
        foreign key (total_plan_id) references t_study_plan_total (id)
            on update cascade,
    constraint fk_study_checkin_user_id
        foreign key (user_id) references t_user_info (id)
            on update cascade
)
    comment '每日打卡记录表' collate = utf8mb4_general_ci;

create index idx_checkin_date
    on t_study_checkin (checkin_date);

create index idx_total_plan_id
    on t_study_checkin (total_plan_id);

create index idx_user_id
    on t_study_checkin (user_id);

create table t_study_plan_stage
(
    id                 bigint unsigned auto_increment comment '主键'
        primary key,
    create_time        datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time        datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_deleted         tinyint  default 0                 not null comment '逻辑删除标识',
    total_plan_id      bigint unsigned                    not null comment '总计划 id',
    stage_type         tinyint  default 1                 not null comment '阶段类型：1基础，2强化，3突破，4冲刺',
    stage_name         varchar(100)                       not null comment '阶段名称',
    stage_start_date   date                               not null comment '阶段开始日期',
    stage_end_date     date                               not null comment '阶段结束日期',
    stage_planned_days int      default 0                 not null comment '阶段计划天数',
    stage_core_task    longtext                           not null comment '阶段核心任务说明',
    create_source      tinyint  default 0                 not null comment '创建来源：0 AI生成/调整，1 人工创建/调整',
    modify_source      tinyint  default 0                 not null comment '修改来源：0 AI生成/调整，1 人工创建/调整',
    stage_status       tinyint  default 1                 not null comment '阶段状态：0停用，1启用',
    constraint uk_total_plan_id_stage_type
        unique (total_plan_id, stage_type),
    constraint fk_plan_stage_total_plan_id
        foreign key (total_plan_id) references t_study_plan_total (id)
            on update cascade
)
    comment '阶段计划表' collate = utf8mb4_general_ci;

create table t_study_plan_day_task
(
    id                      bigint unsigned auto_increment comment '主键'
        primary key,
    create_time             datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time             datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_deleted              tinyint  default 0                 not null comment '逻辑删除标识',
    stage_id                bigint unsigned                    not null comment '阶段计划 id',
    task_date               date                               not null comment '任务日期',
    task_sequence           int      default 1                 not null comment '任务序号（同一天内的第几条）',
    duration_minutes        int      default 0                 not null comment '计划时长（分钟）',
    task_core_goal          varchar(255)                       not null comment '核心目标（可视化展示）',
    task_detail             longtext                           not null comment '任务详细说明',
    task_status             tinyint  default 0                 not null comment '任务状态：0未完成，1已完成，2已跳过',
    actual_duration_minutes int      default 0                 not null comment '实际完成时长（分钟）',
    completion_time         datetime                           null comment '完成时间',
    create_source           tinyint  default 0                 not null comment '创建来源：0 AI生成/调整，1 人工创建/调整',
    modify_source           tinyint  default 0                 not null comment '修改来源：0 AI生成/调整，1 人工创建/调整',
    constraint uk_stage_id_task_date_task_sequence
        unique (stage_id, task_date, task_sequence),
    constraint fk_plan_day_task_stage_id
        foreign key (stage_id) references t_study_plan_stage (id)
            on update cascade
)
    comment '每日任务表' collate = utf8mb4_general_ci;

create index idx_stage_id
    on t_study_plan_day_task (stage_id);

create index idx_task_date
    on t_study_plan_day_task (task_date);

create index idx_task_status
    on t_study_plan_day_task (task_status);

create index idx_stage_status
    on t_study_plan_stage (stage_status);

create index idx_stage_type
    on t_study_plan_stage (stage_type);

create index idx_total_plan_id
    on t_study_plan_stage (total_plan_id);

create index idx_exam_archive_id
    on t_study_plan_total (exam_archive_id);

create index idx_plan_status
    on t_study_plan_total (plan_status);

create index idx_user_id
    on t_study_plan_total (user_id);

create table t_study_progress
(
    id                bigint unsigned auto_increment comment '主键'
        primary key,
    create_time       datetime      default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time       datetime      default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_deleted        tinyint       default 0                 not null comment '逻辑删除标识',
    user_id           bigint unsigned                         not null comment '用户 id',
    total_plan_id     bigint unsigned                         not null comment '总计划 id',
    progress_date     date                                    not null comment '进度日期',
    planned_minutes   int           default 0                 not null comment '计划时长（分钟）',
    completed_minutes int           default 0                 not null comment '已完成时长（分钟）',
    completion_rate   decimal(5, 2) default 0.00              not null comment '完成率（%）',
    remind_status     tinyint       default 0                 not null comment '提醒状态：0未提醒，1已提醒，2忽略/完成后不再提醒',
    remind_count      int           default 0                 not null comment '提醒次数',
    next_remind_time  datetime                                null comment '下一次提醒时间',
    constraint uk_user_id_total_plan_id_progress_date
        unique (user_id, total_plan_id, progress_date),
    constraint fk_study_progress_total_plan_id
        foreign key (total_plan_id) references t_study_plan_total (id)
            on update cascade,
    constraint fk_study_progress_user_id
        foreign key (user_id) references t_user_info (id)
            on update cascade
)
    comment '复习进度跟踪表' collate = utf8mb4_general_ci;

create index idx_progress_date
    on t_study_progress (progress_date);

create index idx_total_plan_id
    on t_study_progress (total_plan_id);

create index idx_user_id
    on t_study_progress (user_id);

create index idx_archive_status
    on t_user_exam_archive (archive_status);

create index idx_user_id
    on t_user_exam_archive (user_id);

create index idx_user_status
    on t_user_info (user_status);

create table t_user_role
(
    id          bigint unsigned auto_increment comment '主键'
        primary key,
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_deleted  tinyint  default 0                 not null comment '逻辑删除标识',
    user_id     bigint unsigned                    not null comment '用户 id',
    role_id     bigint unsigned                    not null comment '角色 id',
    bind_status tinyint  default 1                 not null comment '绑定状态：0停用，1启用',
    constraint uk_user_id_role_id
        unique (user_id, role_id),
    constraint fk_user_role_role_id
        foreign key (role_id) references t_role (id)
            on update cascade,
    constraint fk_user_role_user_id
        foreign key (user_id) references t_user_info (id)
            on update cascade
)
    comment '用户-角色关联表' collate = utf8mb4_general_ci;

create index idx_role_id
    on t_user_role (role_id);

create index idx_user_id
    on t_user_role (user_id);


