# 第三阶段：AI 工具测评模块详细说明

## 1. 模块定位

第三阶段实现的是 AI 工具测评中心模块，对应代码模块为 `modules.tool`。该模块在第一版中的定位是：

```text
工具测评 + 工具推荐 + 工具导航
```

它不是模型平台，也不是外部 AI API 聚合平台，更不是在线生成平台。当前项目不会在后端直接调用豆包、DeepSeek、通义、Kimi、ChatGPT 等工具的外部 API。所有工具信息、适用阶段、评分维度、评分说明都由管理员手动录入和维护。

该模块面向前台用户提供查询能力：

- 查看工具列表。
- 按设计阶段筛选工具。
- 按名称关键词搜索工具。
- 查看工具详情。
- 查看某个工具的维度评分。
- 根据设计阶段查看推荐工具。

该模块面向管理员提供维护能力：

- 新增、修改、删除 AI 工具。
- 设置工具适用的工作流阶段。
- 保存工具在多个测评维度下的评分和说明。

## 2. 与已有模块的关系

### 2.1 与登录/JWT 模块的关系

前台查询接口允许匿名访问，不需要 token。后台管理接口以 `/api/admin/tools` 开头，需要 JWT，并通过 `@PreAuthorize("hasRole('ADMIN')")` 限制管理员访问。

因此，第三阶段没有改变登录和 JWT 业务链路，只复用已有鉴权能力。

### 2.2 与工作流模块的关系

AI 工具需要知道自己适用于哪些建筑设计阶段。这个关系通过 `ai_tool_stage_rel` 表表达，其中：

- `tool_id` 关联 `ai_tool.id`
- `stage_id` 关联 `workflow_stage.id`

例如，一个工具可以同时适用于“前期调研”和“概念设计”。一个阶段也可以推荐多个工具。这是典型的多对多关系。

当前前台接口支持：

```text
GET /api/tools?stageId=1
GET /api/tools/recommend?stageId=1
```

这两个接口都依赖工作流阶段数据。

## 3. 数据库表说明

第三阶段新增 4 张表：

- `ai_tool`
- `ai_tool_stage_rel`
- `evaluation_dimension`
- `ai_tool_evaluation`

### 3.1 `ai_tool`

`ai_tool` 保存 AI 工具的基础信息，是工具测评中心的主表。

字段说明：

- `id`：主键 ID，用于数据库内部唯一定位工具。接口路径中的 `{id}` 使用该字段。
- `name`：工具展示名称，例如“豆包”“DeepSeek”“Kimi”。前台列表和详情页主要展示该字段。
- `code`：工具稳定编码，例如 `DOUBAO`、`DEEPSEEK`。用于程序识别和后续联动，不应随意修改。数据库中已设置唯一索引。
- `official_url`：工具官网或使用入口，用于前台跳转。当前平台只负责导航，不在平台内直接调用工具。
- `logo_url`：工具 Logo 地址。当前初始化数据暂未配置真实 Logo，后续可补充。
- `description`：工具说明，用于介绍工具适合做什么。
- `price_desc`：价格说明，用于展示免费额度、订阅版本或价格备注。
- `version_desc`：版本说明，用于记录 Web、App、模型版本等描述性信息。
- `status`：启用状态。`1` 表示启用，`0` 表示禁用。前台只返回启用工具。
- `create_time`：创建时间，用于后台维护和排查。
- `update_time`：更新时间，用于后台维护和排查。
- `is_deleted`：逻辑删除标记。`0` 表示未删除，`1` 表示已删除。实体类使用 `@TableLogic`。

为什么 `code` 要唯一：

`name` 是展示名，可能随着运营文案变化。`code` 是稳定识别符，后续前端图标映射、数据导入、统计分析或其他模块联动都可以依赖它。唯一约束可以避免同一个工具被重复配置。

### 3.2 `ai_tool_stage_rel`

`ai_tool_stage_rel` 表示工具和工作流阶段之间的适用关系。

字段说明：

- `id`：主键 ID。
- `tool_id`：AI 工具 ID，关联 `ai_tool.id`。
- `stage_id`：工作流阶段 ID，关联 `workflow_stage.id`。
- `create_time`：关系创建时间。

该表设置了 `tool_id + stage_id` 唯一约束，避免同一个工具重复绑定同一个阶段。

当前 SQL 没有使用数据库外键约束，而是采用“普通索引 + 唯一约束 + 服务层校验”的方式维护关系。这样可以避免 DataGrip 在建表脚本中对尚未创建或未同步的表产生解析误报，也更符合当前项目用 MyBatis-Plus 在业务层控制逻辑删除和关联有效性的实现方式。

为什么要用关系表：

一个工具可以适用于多个阶段，例如 ChatGPT 可以用于前期调研、概念设计、方案深化和成果表达。一个阶段也可以推荐多个工具，例如“概念设计”阶段可以推荐豆包、DeepSeek、ChatGPT 等。如果把阶段 ID 用逗号拼接到工具表里，后续查询、筛选、维护都会变复杂，也不符合项目总纲中“不允许用逗号拼字符串表示关系”的要求。

当前后台接口 `POST /api/admin/tools/{id}/stages` 采用覆盖式更新：提交新的 `stageIds` 后，后端先删除该工具旧的阶段关系，再写入新关系。

### 3.3 `evaluation_dimension`

`evaluation_dimension` 保存工具测评维度。

字段说明：

- `id`：主键 ID。
- `name`：维度名称，例如“出图质量”“控制精度”“建筑适配度”。
- `code`：维度稳定编码，例如 `IMAGE_QUALITY`、`CONTROL_ACCURACY`。
- `description`：维度说明，解释该维度评价什么。
- `sort_order`：展示顺序。前台评分列表按该字段升序返回。
- `status`：启用状态。前台评分列表只使用启用维度。
- `create_time`：创建时间。
- `update_time`：更新时间。

为什么评分维度要单独建表：

评分维度不是代码常量，而是可维护的数据。后续团队可能调整维度名称、顺序、说明，甚至新增或禁用某个维度。如果写死在代码里，每次调整都要改代码、重新编译、重新部署。拆成表后，维度可以通过数据库或后续后台管理能力维护。

### 3.4 `ai_tool_evaluation`

`ai_tool_evaluation` 保存某个工具在某个维度下的评分和说明。

字段说明：

- `id`：主键 ID。
- `tool_id`：AI 工具 ID，关联 `ai_tool.id`。
- `dimension_id`：测评维度 ID，关联 `evaluation_dimension.id`。
- `score`：评分，数据库类型为 `DECIMAL(4,1)`，支持一位小数。
- `comment`：评分说明，用于解释为什么给出该分数。
- `create_time`：创建时间。
- `update_time`：更新时间。

该表设置了 `tool_id + dimension_id` 唯一约束，表示一个工具在同一个维度下只能有一条评分记录。

当前 SQL 没有为 `tool_id` 和 `dimension_id` 添加数据库外键约束。后端在保存评分时会校验工具和维度是否存在，数据库层通过唯一索引防止同一工具同一维度出现重复评分。

为什么工具表和评分表拆开：

如果把所有评分字段直接放在 `ai_tool` 中，例如 `image_quality_score`、`control_accuracy_score`，后续每新增一个维度都要改表结构和代码。拆成评分表后，工具和维度形成可扩展关系，新增维度只需要往 `evaluation_dimension` 插入数据，再在 `ai_tool_evaluation` 保存评分。

## 4. 初始化数据说明

初始化 SQL 文件为：

```text
sql/ai_tool_init_data.sql
```

当前初始化数据只用于开发、联调和演示，不是最终测评结论。

初始化工具包括：

- 豆包
- DeepSeek
- 通义
- Kimi
- 智谱清言
- ChatGPT

初始化维度包括：

- 出图质量
- 控制精度
- 建筑适配度
- 易用性
- 响应速度
- 学习成本
- 成本/价格
- 稳定性

初始化数据还包含部分工具与阶段关系、部分工具评分。后续正式定稿时，应根据团队真实测评结果调整：

- 工具说明
- 官网入口
- 适用阶段
- 评分分值
- 评分说明
- 是否启用

## 5. 接口说明

### 5.1 前台接口

前台接口允许匿名访问，只返回启用且未删除的工具。

#### 5.1.1 查询工具列表

```text
GET /api/tools
```

可选参数：

- `stageId`：按工作流阶段筛选。
- `keyword`：按工具名称模糊搜索。

示例：

```text
GET /api/tools
GET /api/tools?stageId=1
GET /api/tools?keyword=Deep
GET /api/tools?stageId=2&keyword=豆包
```

返回工具基础信息和适用阶段列表。

#### 5.1.2 查询工具详情

```text
GET /api/tools/{id}
```

用于查询单个工具详情。只允许查询启用且未逻辑删除的工具。

#### 5.1.3 查询工具评分

```text
GET /api/tools/{id}/evaluations
```

返回某个工具的评分维度列表，包括：

- 维度 ID
- 维度名称
- 维度编码
- 维度说明
- 排序号
- 分数
- 评分说明

结果按 `evaluation_dimension.sort_order` 升序返回。

#### 5.1.4 根据阶段推荐工具

```text
GET /api/tools/recommend?stageId=1
```

该接口根据阶段 ID 查询适用于该阶段的工具。当前推荐逻辑是基于管理员维护的阶段关系，不包含算法推荐，也不调用外部 AI API。

### 5.2 后台接口

后台接口需要 JWT，并要求 `ADMIN` 角色。

#### 5.2.1 新增工具

```text
POST /api/admin/tools
```

用于新增 AI 工具。`name` 和 `code` 必填，`code` 必须唯一。

#### 5.2.2 修改工具

```text
PUT /api/admin/tools/{id}
```

用于修改工具基础信息、价格说明、版本说明、启用状态等。

#### 5.2.3 删除工具

```text
DELETE /api/admin/tools/{id}
```

使用逻辑删除，不物理删除工具记录。删除后前台接口不再返回该工具。

#### 5.2.4 设置工具适用阶段

```text
POST /api/admin/tools/{id}/stages
```

当前实现为覆盖式更新。请求体中的 `stageIds` 会成为该工具新的完整适用阶段集合。

#### 5.2.5 保存工具评分

```text
POST /api/admin/tools/{id}/evaluations
```

支持一次保存多个维度评分。已有评分会更新，不存在的评分会新增。

## 6. 权限与安全

公开接口：

```text
GET /api/tools
GET /api/tools/{id}
GET /api/tools/{id}/evaluations
GET /api/tools/recommend
```

后台接口：

```text
POST /api/admin/tools
PUT /api/admin/tools/{id}
DELETE /api/admin/tools/{id}
POST /api/admin/tools/{id}/stages
POST /api/admin/tools/{id}/evaluations
```

第二阶段之后，`SecurityConfig` 在第三阶段只做了最小变更：放行前台工具查询接口。后台接口没有放行，仍走 JWT 和管理员权限校验。

## 7. 为什么第一版不接外部 AI API

根据项目总纲，第一版 AI 工具模块的定位是工具测评、工具推荐和工具导航，而不是在线调用生成服务。

不接外部 AI API 的原因：

1. 降低项目复杂度，先保证平台可运行、可展示、可维护。
2. 避免不同工具 API 鉴权、计费、限流、稳定性带来的额外问题。
3. 当前业务重点是“帮助用户选择工具”，不是“替用户调用工具”。
4. 管理员手动录入测评信息更适合大创项目第一版落地。
5. 后续如果需要接 API，可以在工具基础数据稳定后单独设计扩展模块。

## 8. 后续正式定稿时建议修改的地方

正式定稿前建议重点检查：

- `ai_tool.description` 是否准确。
- `ai_tool.official_url` 是否是最终使用入口。
- `ai_tool.logo_url` 是否补充正式图标。
- `ai_tool_stage_rel` 是否符合团队最终的工作流阶段划分。
- `evaluation_dimension` 的维度名称和说明是否最终确定。
- `ai_tool_evaluation.score` 是否来自真实测评。
- `ai_tool_evaluation.comment` 是否能支撑答辩说明。
- `status` 是否正确控制前台展示。

初始化 SQL 可以继续保留作为开发基线，但真实环境变更建议单独写迁移 SQL，避免误覆盖已有维护数据。

## 9. 相关文件

SQL：

- `sql/ai_tool_schema.sql`
- `sql/ai_tool_init_data.sql`

Controller：

- `src/main/java/com/project/modules/tool/controller/AiToolController.java`
- `src/main/java/com/project/modules/tool/controller/AdminAiToolController.java`

Service：

- `src/main/java/com/project/modules/tool/service/AiToolService.java`
- `src/main/java/com/project/modules/tool/service/impl/AiToolServiceImpl.java`

Entity：

- `src/main/java/com/project/modules/tool/entity/AiTool.java`
- `src/main/java/com/project/modules/tool/entity/AiToolStageRel.java`
- `src/main/java/com/project/modules/tool/entity/EvaluationDimension.java`
- `src/main/java/com/project/modules/tool/entity/AiToolEvaluation.java`

DTO：

- `src/main/java/com/project/modules/tool/dto/AiToolCreateRequest.java`
- `src/main/java/com/project/modules/tool/dto/AiToolUpdateRequest.java`
- `src/main/java/com/project/modules/tool/dto/ToolStageSetRequest.java`
- `src/main/java/com/project/modules/tool/dto/ToolEvaluationSaveRequest.java`

VO：

- `src/main/java/com/project/modules/tool/vo/AiToolVO.java`
- `src/main/java/com/project/modules/tool/vo/ToolStageVO.java`
- `src/main/java/com/project/modules/tool/vo/AiToolEvaluationVO.java`

Mapper：

- `src/main/java/com/project/modules/tool/mapper/AiToolMapper.java`
- `src/main/java/com/project/modules/tool/mapper/AiToolStageRelMapper.java`
- `src/main/java/com/project/modules/tool/mapper/EvaluationDimensionMapper.java`
- `src/main/java/com/project/modules/tool/mapper/AiToolEvaluationMapper.java`
