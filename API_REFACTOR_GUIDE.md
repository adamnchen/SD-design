# API 重构指南

## 重构概述

为了解决客户端和后台管理接口混乱的问题，我们对用户个人信息相关的接口进行了重构，将客户端功能和后台管理功能进行了清晰的分离。

## 接口变化对比

### 1. 客户端接口 (新增)

**基础路径**: `/api/user/profile`

| 功能 | 原路径 | 新路径 | 方法 |
|------|--------|--------|------|
| 获取个人信息 | `/system/user/profile` | `/api/user/profile` | GET |
| 修改个人信息 | `/system/user/profile` | `/api/user/profile` | PUT |
| 修改密码 | `/system/user/profile/updatePwd` | `/api/user/profile/updatePwd` | PUT |
| 头像上传 | `/system/user/profile/avatar` | `/api/user/profile/avatar` | POST |
| 添加地址 | `/system/user/profile/addAddress` | `/api/user/profile/address` | POST |
| 删除地址 | `/system/user/profile/deleteAddress/{id}` | `/api/user/profile/address/{id}` | DELETE |
| 修改地址 | `/system/user/profile/updateAddress` | `/api/user/profile/address` | PUT |
| 获取地址详情 | `/system/user/profile/getAddress/{id}` | `/api/user/profile/address/{id}` | GET |
| 获取地址列表 | `/system/user/profile/getAddressList` | `/api/user/profile/address` | GET |
| 设置默认地址 | `/system/user/profile/setDefaultAddress/{id}` | `/api/user/profile/address/{id}/default` | PUT |
| 获取省市区 | `/system/user/profile/getArea` | `/api/user/profile/address/area` | GET |
| 添加标签 | `/system/user/profile/addTag` | `/api/user/profile/tag` | POST |
| 删除标签 | `/system/user/profile/{tagId}` | `/api/user/profile/tag/{tagId}` | DELETE |
| 修改标签 | `/system/user/profile/updateTag` | `/api/user/profile/tag` | PUT |
| 获取标签列表 | `/system/user/profile/list` | `/api/user/profile/tag` | GET |
| 获取标签详情 | `/system/user/profile/{tagId}` | `/api/user/profile/tag/{tagId}` | GET |

### 2. 后台管理接口 (保留)

**基础路径**: `/system/user/profile`

| 功能 | 路径 | 方法 | 说明 |
|------|------|------|------|
| 获取个人信息 | `/system/user/profile` | GET | 后台管理员查看个人信息 |
| 修改个人信息 | `/system/user/profile` | PUT | 后台管理员修改个人信息 |
| 修改密码 | `/system/user/profile/updatePwd` | PUT | 后台管理员修改密码 |
| 头像上传 | `/system/user/profile/avatar` | POST | 后台管理员上传头像 |

## 权限控制

### 客户端接口
- **路径前缀**: `/api/`
- **权限**: `@SaCheckLogin` - 需要用户登录
- **用途**: 客户端用户使用

### 后台管理接口
- **路径前缀**: `/system/`
- **权限**: `@SaCheckLogin` - 需要管理员登录
- **用途**: 后台管理系统使用

## 迁移指南

### 前端代码迁移

1. **更新API基础路径**
   ```javascript
   // 原代码
   const API_BASE = '/system/user/profile';
   
   // 新代码
   const API_BASE = '/api/user/profile';
   ```

2. **更新具体接口路径**
   ```javascript
   // 地址管理接口示例
   // 原代码
   GET /system/user/profile/getAddressList
   POST /system/user/profile/addAddress
   
   // 新代码
   GET /api/user/profile/address
   POST /api/user/profile/address
   ```

3. **标签管理接口示例**
   ```javascript
   // 原代码
   GET /system/user/profile/list
   POST /system/user/profile/addTag
   
   // 新代码
   GET /api/user/profile/tag
   POST /api/user/profile/tag
   ```

### 后端代码迁移

1. **Controller层**
   - 客户端功能已迁移到 `UserProfileController`
   - 后台管理功能保留在 `SysProfileController`

2. **Service层**
   - 无需修改，Service层代码保持不变

3. **权限配置**
   - 客户端接口使用 `/api/` 路径前缀
   - 后台管理接口使用 `/system/` 路径前缀

## 优势

1. **职责清晰**: 客户端和后台管理功能完全分离
2. **路径语义化**: API路径更加直观易懂
3. **权限控制**: 不同角色访问不同的接口
4. **维护性**: 代码结构更加清晰，便于维护
5. **扩展性**: 未来可以独立扩展客户端和后台管理功能

## 注意事项

1. **向后兼容**: 原有的 `/system/` 路径接口仍然保留，但建议逐步迁移到新的 `/api/` 路径
2. **权限验证**: 确保客户端接口只允许普通用户访问，后台管理接口只允许管理员访问
3. **文档更新**: 更新API文档，明确标注客户端和后台管理接口的区别
4. **测试覆盖**: 确保所有接口都有相应的测试用例

## 服务层重构

### 服务接口重命名
为了明确服务职责，我们对服务接口进行了重命名：

| 原服务接口 | 新服务接口 | 说明 |
|-----------|-----------|------|
| `ISysUserAddressService` | `IUserAddressService` | 用户地址管理服务 |
| `ISysUserTagService` | `IUserTagService` | 用户标签管理服务 |

### 服务实现类
- `SysUserAddressServiceImpl` → `UserAddressServiceImpl`
- `SysUserTagServiceImpl` → `UserTagServiceImpl`

### 服务职责明确化
- **系统管理服务** (`ISys*Service`): 系统管理员使用的功能
- **用户业务服务** (`IUser*Service`): 客户端用户使用的功能

## 后续计划

1. 逐步废弃旧的 `/system/` 路径下的客户端功能
2. 完善权限控制，确保接口安全性
3. 优化接口设计，提供更好的用户体验
4. 建立完整的API文档和测试用例
5. **考虑创建独立的用户业务模块** (`sutran-sd-module-user`)
