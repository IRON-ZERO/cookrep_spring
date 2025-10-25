---
application.yml을 생성 후 복사해서 사용하시면 됩니다.
---
server:  
&nbsp;&nbsp;  port: 8080  
spring:  
&nbsp;&nbsp;  application:  
&nbsp;&nbsp;&nbsp;&nbsp;    name: cookrep_spring  
&nbsp;&nbsp;  datasource:  
&nbsp;&nbsp;&nbsp;&nbsp;    url: jdbc:mysql://localhost:3306/cookrep?characterEncoding=utf8&serverTimezone=Asia/Seoul  
&nbsp;&nbsp;&nbsp;&nbsp;    username: 여기에 아이디  
&nbsp;&nbsp;&nbsp;&nbsp;    password: 여기에 비번   
&nbsp;&nbsp;&nbsp;&nbsp;    driver-class-name: com.mysql.cj.jdbc.Driver  
&nbsp;&nbsp;    hikari:  
&nbsp;&nbsp;&nbsp;&nbsp;      minimum-idle: 5 # 최소 커넥션   
&nbsp;&nbsp;&nbsp;&nbsp;      maximum-pool-size: 10 # 최대 커넥션   
&nbsp;&nbsp;&nbsp;&nbsp;      connection-timeout: 30000 # 30초 이상 대기시 에러  
&nbsp;&nbsp;  jpa:  
&nbsp;&nbsp;&nbsp;&nbsp;    hibernate:  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;      ddl-auto: update  
&nbsp;&nbsp;&nbsp;&nbsp;    show_sql: true  
&nbsp;&nbsp;&nbsp;&nbsp;    properties:  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;      hibernate:  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;        format_sql: true  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;        use_sql_comments: true  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;        dialect: org.hibernate.dialect.MySQL8Dialect
