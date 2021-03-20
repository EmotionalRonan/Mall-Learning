# Spring Boot 项目整合 MyBatis

### 项目目录结构：





### 1. 创建新的 `Spring Boot` 项目

在 `pom.xml` 中添加 `MyBatis` 和 `MySQL数据库` 依赖
   ```xml
        <dependencies>
            <!--SpringBoot通用依赖模块-->
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-web</artifactId>
            </dependency>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-test</artifactId>
                <scope>test</scope>
            </dependency>
            <!--MyBatis分页插件-->
            <dependency>
                <groupId>com.github.pagehelper</groupId>
                <artifactId>pagehelper-spring-boot-starter</artifactId>
                <version>1.2.10</version>
            </dependency>
            <!-- MyBatis 生成器 -->
            <dependency>
                <groupId>org.mybatis.generator</groupId>
                <artifactId>mybatis-generator-core</artifactId>
                <version>1.3.3</version>
            </dependency>
            <!--Mysql数据库驱动-->
            <dependency>
                <groupId>mysql</groupId>
                <artifactId>mysql-connector-java</artifactId>
                <version>8.0.15</version>
            </dependency>
        </dependencies>

   ```
### 2. 修改 `SpringBoot` 配置文件
在 `application.yml` 中添加 `数据源配置` 和 `MyBatis 的 mapper.xml` 的路径配置
   ```yaml
server:
   port: 8080

spring:
   datasource:
   url: jdbc:mysql://localhost:3306/mall?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
   username: root
   password: root
   driver-class-name: com.mysql.cj.jdbc.Driver

mybatis:
   mapper-locations:
   - classpath:mapper/*.xml
   - classpath*:com/**/mapper/*.xml
   ```
   或者 在 `application.properties` 中添加 以上配置
   ```properties
server.port=8080

spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/mall?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=root

mybatis.mapper-locations.classpath=mapper/*.xml
mybatis.mapper-locations.classpath*=com/**/mapper/*.xml
   ```
### 3. 添加 `MyBatis` 配置文件
在 `resources` 中创建 `generatorConfig.xml` 配置文件，`jdbcConnection` 标签中 指定`application.properties` 的数据库驱动、数据库url、 账号、密码
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE generatorConfiguration
     PUBLIC "-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN"
     "http://mybatis.org/dtd/mybatis-generator-config_1_0.dtd">

<generatorConfiguration>
     <properties resource="application.properties"/>
     <context id="MySqlContext" targetRuntime="MyBatis3" defaultModelType="flat">
        <property name="beginningDelimiter" value="`"/>
        <property name="endingDelimiter" value="`"/>
        <property name="javaFileEncoding" value="UTF-8"/>
        <!-- 为模型生成序列化方法-->
        <plugin type="org.mybatis.generator.plugins.SerializablePlugin"/>
        <!-- 为生成的Java模型创建一个toString方法 -->
        <plugin type="org.mybatis.generator.plugins.ToStringPlugin"/>
        <!--可以自定义生成model的代码注释-->
	    <commentGenerator type="tk.iovr.mall_learning001.mybatisgenerator.CommentGenerator">
             <!-- 是否去除自动生成的注释 true：是 ： false:否 -->
             <property name="suppressAllComments" value="true"/>
             <property name="suppressDate" value="true"/>
             <property name="addRemarkComments" value="true"/>
        </commentGenerator>

        <!--配置数据库连接-->
        <jdbcConnection driverClass="${spring.datasource.driver-class-name}"
                        connectionURL="${spring.datasource.url}"
                        userId="${spring.datasource.username}"
                        password="${spring.datasource.password}">
             <!--解决mysql驱动升级到8.0后不生成指定数据库代码的问题-->
             <property name="nullCatalogMeansCurrent" value="true" />
        </jdbcConnection>

        <!--指定生成model的路径-->
        <javaModelGenerator targetPackage="tk.iovr.mall_learning001.mybatisgenerator.model" targetProject="src/main/java"/>
        <!--指定生成mapper.xml的路径-->
        <sqlMapGenerator targetPackage="tk.iovr.mall_learning001.mybatisgenerator.mapper" targetProject="src/main/resources"/>
        <!--指定生成mapper接口的的路径-->
        <javaClientGenerator type="XMLMAPPER" targetPackage="tk.iovr.mall_learning001.mybatisgenerator.mapper"
                                    targetProject="src/main/java"/>

        <!--生成全部表tableName设为%-->
        <!-- 生成 pms_brand 表mapper  对应类名PmsBrand -->
        <table tableName="pms_brand" domainObjectName="PmsBrand">
            <generatedKey column="id" sqlStatement="MySql" identity="true"/>
        </table>
        <!-- 生成 cms_help 表mapper   对应类名CmsHelp -->
        <table tableName="cms_help" domainObjectName="CmsHelp">
            <generatedKey column="id" sqlStatement="MySql" identity="true"/>
        </table>
      </context>
</generatorConfiguration>
```
### 4. 编写 `CommentGenerator` 注释生成器类
```java
   // 自定义注释生成器
	public class CommentGenerator  extends DefaultCommentGenerator {
       private boolean addRemarkComments = false;
       // 设置用户配置的参数
       @Override
       public void addConfigurationProperties(Properties properties) {
           super.addConfigurationProperties(properties);
           this.addRemarkComments = StringUtility.isTrue(properties.getProperty("addRemarkComments"));
       }
       // 给字段添加注释
       @Override
       public void addFieldComment(Field field, IntrospectedTable introspectedTable,
                                   IntrospectedColumn introspectedColumn) {
           String remarks = introspectedColumn.getRemarks();
           // 根据参数和备注信息判断是否添加备注信息
           if (addRemarkComments && StringUtility.stringHasValue(remarks)) {
               addFieldJavaDoc(field, remarks);
           }
       }
       // 给model的字段添加注释
       private void addFieldJavaDoc(Field field, String remarks) {
           // 文档注释开始
           field.addJavaDocLine("/**");
           // 获取数据库字段的备注信息
           String[] remarkLines = remarks.split(System.getProperty("line.separator"));
           for (String remarkLine : remarkLines) {
               field.addJavaDocLine(" * " + remarkLine);
           }
           addJavadocTag(field, false);
           field.addJavaDocLine(" */");
       }
	}
```
### 5. 生成 Mapper接口、Mapper.xml配置、Model实体类

1. 通过编写 `Generator` 类，根据数据库表 自动生成 `model`、`mapper.xml`、`mapper接口` 和 `Example`，通常情况下的单表查询不用再手写 Mapper

 ```java
    public class Generator {
        public static void main(String[] args) throws Exception {
            //MBG 执行过程中的警告信息
            List<String> warnings = new ArrayList<String>();
            //当生成的代码重复时，覆盖原代码
            boolean overwrite = true;
            //读取我们的 MBG 配置文件
            InputStream is = Generator.class.getResourceAsStream("/generatorConfig.xml");
            ConfigurationParser cp = new ConfigurationParser(warnings);
            Configuration config = cp.parseConfiguration(is);
            is.close();
            DefaultShellCallback callback = new DefaultShellCallback(overwrite);
            //创建 MBG
            MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
            //执行生成代码
            myBatisGenerator.generate(null);
            //输出警告信息
            for (String warning : warnings) {
                System.out.println(warning);
            }
        }
    }
 ```

2. 通过执行 `mvn mybatis-generator:generate -e`  命令生成,

   在 `pom.xml` 中 添加 `mybatis-generator` 插件
   
```xml
   	<plugin>
   				<groupId>org.mybatis.generator</groupId>
   				<artifactId>mybatis-generator-maven-plugin</artifactId>
   				<version>1.3.2</version>
   				<executions>
   					<execution>
   						<id>Generate MyBatis Artifacts</id>
   						<goals>
   							<goal>
   								generate
   							</goal>
   						</goals>
   					</execution>
   				</executions>
   				<configuration>
   					<verbose>true</verbose>
   					<overwrite>true</overwrite>
   				</configuration>
   	</plugin>
```

   

### 6. 编写业务逻辑

#### 1. 编写 Mybatis 配置加载类 `MyBatisConfig.java`
​	添加注解 `@Configuration` 声明是配置类，`@MapperScan()` 注解指定 mapper 路径，Spring 会自动加载。

   ```java
   @Configuration // 声明是 MyBatis 配置类
   @MapperScan("tk.iovr.mall_learning001.mybatisgenerator.mapper")
   public class MyBatisConfig {}
   ```

#### 2. 定义某个业务的 service接口 `PmsBrandService.java` 
指定业务都有哪些处理方法

   ```java
   public interface PmsBrandService {
       List<PmsBrand> listAllBrand();											// 获取所有 Brand
       int createBrand(PmsBrand brand);										// 创建新的 Brand
       int updateBrand(Long id, PmsBrand brand);						// 更新指定 ID 的 Brand
       int deleteBrand(Long id);														// 删除指定 ID 的 Brand
       List<PmsBrand> listBrand(int pageNum, int pageSize);// 分页获取 Brand 列表
       PmsBrand getBrand(Long id);													// 获取指定 ID 的 Brand		
   }
   ```

#### 3. 实现 某个业务的 service 具体实现 `PmsBrandServiceImpl.java`

   ```java
   // PmsBrandService实现类
   @Service
   public class PmsBrandServiceImpl  implements PmsBrandService {
   
       @Autowired
       private PmsBrandMapper brandMapper;
   
       @Override
       public List<PmsBrand> listAllBrand() {
           return brandMapper.selectByExample(new PmsBrandExample());
       }
   
       @Override
       public int createBrand(PmsBrand brand) {
           return brandMapper.insertSelective(brand);
       }
   
       @Override
       public int updateBrand(Long id, PmsBrand brand) {
           brand.setId(id);
           return brandMapper.updateByPrimaryKeySelective(brand);    }
   
       @Override
       public int deleteBrand(Long id) {
           return brandMapper.deleteByPrimaryKey(id);
       }
   
       //分页查找
       @Override
       public List<PmsBrand> listBrand(int pageNum, int pageSize) {
           PageHelper.startPage(pageNum, pageSize);
           return brandMapper.selectByExample(new PmsBrandExample());    }
   
       @Override
       public PmsBrand getBrand(Long id) {
           return brandMapper.selectByPrimaryKey(id);
       }
   }
   ```

#### 4. 定义 通用返回值接口

   - 定义返回码 `IErrorCode接口` 及 `ResultCode实现 `

   ```java
   //封装API的错误码
   public interface IErrorCode {
       long getCode();
       String getMessage();
   }
   ```

   ```java
   public enum ResultCode implements IErrorCode {
     
       SUCCESS(200, "操作成功"),
       FAILED(500, "操作失败"),
       VALIDATE_FAILED(404, "参数检验失败"),
       UNAUTHORIZED(401, "暂未登录或token已经过期"),
       FORBIDDEN(403, "没有相关权限");
     
       private long code;
       private String message;
       private ResultCode(long code, String message) {
           this.code = code;
           this.message = message;
       }
       public long getCode() {
           return code;
       }
       public String getMessage() {
           return message;
       }
   }
   ```

   - 定义 公共` CommonResult 返回值`类

   ```java
   public class CommonResult<T> {
       private long code;
       private String message;
       private T data;
     
       protected CommonResult() {}
       protected CommonResult(long code, String message, T data) {
           this.code = code;
           this.message = message;
           this.data = data;
       }
       // 省略 getter setter 方法
     
       // 成功返回结果， @param data 获取的数据
       public static <T> CommonResult<T> success(T data) {
           return new CommonResult<T>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
       }
      // 成功返回结果， @param data 获取的数据， @param  message 提示信息
       public static <T> CommonResult<T> success(T data, String message) {
           return new CommonResult<T>(ResultCode.SUCCESS.getCode(), message, data);
       }
      //失败返回结果， @param errorCode 错误码
       public static <T> CommonResult<T> failed(IErrorCode errorCode) {
           return new CommonResult<T>(errorCode.getCode(), errorCode.getMessage(), null);
       }
      // 失败返回结果， @param message 提示信息
       public static <T> CommonResult<T> failed(String message) {
           return new CommonResult<T>(ResultCode.FAILED.getCode(), message, null);
       }
      // 失败返回结果
       public static <T> CommonResult<T> failed() {
           return failed(ResultCode.FAILED);
       }
   
      // 参数验证失败返回结果
       public static <T> CommonResult<T> validateFailed() {
           return failed(ResultCode.VALIDATE_FAILED);
       }
      // 参数验证失败返回结果， @param message 提示信息
       public static <T> CommonResult<T> validateFailed(String message) {
           return new CommonResult<T>(ResultCode.VALIDATE_FAILED.getCode(), message, null);
       }
      // 未登录返回结果
       public static <T> CommonResult<T> unauthorized(T data) {
           return new CommonResult<T>(ResultCode.UNAUTHORIZED.getCode(), ResultCode.UNAUTHORIZED.getMessage(), data);
       }
      // 未授权返回结果
       public static <T> CommonResult<T> forbidden(T data) {
           return new CommonResult<T>(ResultCode.FORBIDDEN.getCode(), ResultCode.FORBIDDEN.getMessage(), data);
       }
   }
   
   ```

#### 5. 实现某个业务的 `PmsBrandController.java` ,响应请求
```java
	  // PmsBrand 管理 Controller
	  @Controller //声明 是控制类，
	  @RequestMapping("/brand")
	  public class PmsBrandController {
	      @Autowired
	      private PmsBrandService demoService;
	    // 定义 slf4j   log 
	      private static final Logger LOGGER = LoggerFactory.getLogger(PmsBrandController.class);
	    //查询所有品牌信息
	      @RequestMapping(value = "/listAll", method = RequestMethod.GET)
	      @ResponseBody
	      public CommonResult<List<PmsBrand>> getBrandList() {
	          return CommonResult.success(demoService.listAllBrand());
	      }
	    //创建新的品牌信息
	      @RequestMapping(value = "/create", method = RequestMethod.POST)
	      @ResponseBody
	      public CommonResult createBrand(@RequestBody PmsBrand pmsBrand) {
	          CommonResult commonResult;
	          int count = demoService.createBrand(pmsBrand);
	          if (count == 1) {
	              commonResult = CommonResult.success(pmsBrand);
	              LOGGER.debug("createBrand success:{}", pmsBrand);
	          } else {
	              commonResult = CommonResult.failed("操作失败");
	              LOGGER.debug("createBrand failed:{}", pmsBrand);
	          }
	          return commonResult;
	      }
	    //更新指定ID的品牌信息 
	      @RequestMapping(value = "/update/{id}", method = RequestMethod.POST)
	      @ResponseBody
	      public CommonResult updateBrand(@PathVariable("id") Long id, @RequestBody PmsBrand pmsBrandDto, BindingResult result) {
	          CommonResult commonResult;
	          int count = demoService.updateBrand(id, pmsBrandDto);
	          if (count == 1) {
	              commonResult = CommonResult.success(pmsBrandDto);
	              LOGGER.debug("updateBrand success:{}", pmsBrandDto);
	          } else {
	              commonResult = CommonResult.failed("操作失败");
	              LOGGER.debug("updateBrand failed:{}", pmsBrandDto);
	          }
	          return commonResult;
	      }
	    //删除指定ID的品牌信息
	      @RequestMapping(value = "/delete/{id}", method = RequestMethod.GET)
	      @ResponseBody
	      public CommonResult deleteBrand(@PathVariable("id") Long id) {
	          int count = demoService.deleteBrand(id);
	          if (count == 1) {
	              LOGGER.debug("deleteBrand success :id={}", id);
	              return CommonResult.success(null);
	          } else {
	              LOGGER.debug("deleteBrand failed :id={}", id);
	              return CommonResult.failed("操作失败");
	          }
	      }
	    // 分页 查找品牌信息
	      @RequestMapping(value = "/list", method = RequestMethod.GET)
	      @ResponseBody
	      public CommonResult<CommonPage<PmsBrand>> listBrand(
	        								 @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
	                         @RequestParam(value = "pageSize", defaultValue = "3") Integer pageSize) {
	          List<PmsBrand> brandList = demoService.listBrand(pageNum, pageSize);
	          return CommonResult.success(CommonPage.restPage(brandList));
	      }
	    // 查找指定ID的品牌信息
	      @RequestMapping(value = "/{id}", method = RequestMethod.GET)
	      @ResponseBody
	      public CommonResult<PmsBrand> brand(@PathVariable("id") Long id) {
	          return CommonResult.success(demoService.getBrand(id));
	      }
	  }
```

### 7. 运行 Application 主类， 进行接口测试

```shell
http://localhost:8080/brand/list
```





## 手动编写 Mapper 流程

