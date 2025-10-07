# 服务层重构方案

## 当前问题分析

### 1. 模块职责混乱
- `sutran-sd-module-system` 模块包含了用户业务功能
- 服务层职责不清晰，混合了系统管理和用户业务

### 2. 服务设计问题
- `ISysUserAddressService` - 用户地址服务放在 system 模块
- `ISysUserTagService` - 用户标签服务放在 system 模块
- 这些应该是用户业务功能，不是系统管理功能

## 重构方案

### 方案1：创建用户业务模块 (推荐)

#### 1.1 创建新模块
```
sutran-sd-modules/
├── sutran-sd-module-system/          # 系统管理模块
│   ├── ISysUserService              # 系统用户管理
│   ├── ISysDeptService              # 部门管理
│   ├── ISysRoleService              # 角色管理
│   └── ISysMenuService              # 菜单管理
└── sutran-sd-module-user/           # 用户业务模块 (新建)
    ├── IUserProfileService          # 用户个人信息
    ├── IUserAddressService          # 用户地址管理
    ├── IUserTagService              # 用户标签管理
    └── IUserMemberService           # 用户会员管理
```

#### 1.2 服务接口设计
```java
// 用户业务模块 - 用户个人信息服务
public interface IUserProfileService {
    // 获取个人信息
    UserProfileVO getUserProfile(Long userId);
    
    // 更新个人信息
    void updateUserProfile(Long userId, UserProfileUpdateDTO dto);
    
    // 修改密码
    void changePassword(Long userId, PasswordChangeDTO dto);
    
    // 上传头像
    String uploadAvatar(Long userId, MultipartFile avatar);
}

// 用户业务模块 - 用户地址服务
public interface IUserAddressService {
    // 添加地址
    void addAddress(Long userId, AddressDTO address);
    
    // 删除地址
    void deleteAddress(Long userId, Long addressId);
    
    // 更新地址
    void updateAddress(Long userId, AddressDTO address);
    
    // 获取地址列表
    List<AddressVO> getAddressList(Long userId);
    
    // 设置默认地址
    void setDefaultAddress(Long userId, Long addressId);
}

// 用户业务模块 - 用户标签服务
public interface IUserTagService {
    // 添加标签
    void addTag(Long userId, TagDTO tag);
    
    // 删除标签
    void deleteTag(Long userId, Long tagId);
    
    // 更新标签
    void updateTag(Long userId, TagDTO tag);
    
    // 获取标签列表
    List<TagVO> getTagList(Long userId);
}
```

### 方案2：重构现有模块

#### 2.1 保持现有模块结构，但重新组织服务
```
sutran-sd-module-system/
├── service/
│   ├── system/                      # 系统管理服务
│   │   ├── ISysUserService         # 系统用户管理
│   │   ├── ISysDeptService         # 部门管理
│   │   └── ISysRoleService         # 角色管理
│   └── user/                       # 用户业务服务
│       ├── IUserProfileService     # 用户个人信息
│       ├── IUserAddressService      # 用户地址管理
│       └── IUserTagService          # 用户标签管理
```

#### 2.2 重命名现有服务
```java
// 重命名现有服务，明确职责
ISysUserAddressService -> IUserAddressService
ISysUserTagService -> IUserTagService

// 创建新的用户个人信息服务
IUserProfileService -> 用户个人信息管理
```

## 推荐方案：方案1

### 优势
1. **职责清晰**：系统管理和用户业务完全分离
2. **模块独立**：可以独立开发、测试、部署
3. **扩展性好**：未来可以轻松添加新的用户业务功能
4. **维护性强**：代码结构清晰，便于维护

### 实施步骤

#### 步骤1：创建用户业务模块
```bash
# 创建新模块目录
mkdir sutran-sd-modules/sutran-sd-module-user
```

#### 步骤2：迁移服务接口
- 将 `ISysUserAddressService` 迁移到 `IUserAddressService`
- 将 `ISysUserTagService` 迁移到 `IUserTagService`
- 创建新的 `IUserProfileService`

#### 步骤3：更新Controller依赖
- 更新 `UserProfileController` 的依赖注入
- 更新 `SysProfileController` 的依赖注入

#### 步骤4：更新模块依赖
- 在 `pom.xml` 中添加新模块依赖
- 更新模块间的依赖关系

## 具体实施建议

### 1. 立即可以做的
- 重命名现有服务接口，明确职责
- 更新Controller中的服务注入

### 2. 后续优化
- 创建独立的用户业务模块
- 完善服务层设计
- 添加更细粒度的权限控制

## 总结

当前的服务层确实需要重构，主要问题是：
1. **职责混乱**：系统管理和用户业务混在一起
2. **模块设计不合理**：用户业务功能放在系统管理模块中
3. **服务命名不清晰**：`ISysUserAddressService` 应该是 `IUserAddressService`

建议采用方案1，创建独立的用户业务模块，这样架构会更加清晰和合理。
