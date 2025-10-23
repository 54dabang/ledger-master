##台账系统
### 打包命令
~~~shel
mvn clean package -Dmaven.test.skip=true
~~~

### 请求token示例
~~~curl
 http://localhost:3020/chat/share?shareId=8fymqruveg60zv93drc1n6np&projectId=10&token=Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJsaXVfY2hhbzEwIiwibG9naW5fdXNlcl9rZXkiOiJjMjMzNjg3ZC03MDc3LTRlNTItOGIxMy1iNDE4MGE2ZGEzNGMifQ.GF1A4mpfkk4zn0MZLel2GK9-F58Lax7ycN_QCp_TnRA4mT8XVTnK1RRpxhkt01-mwNgW0o40hU9PpL-6DAal2Q
~~~

# 数据源配置
#打包部署
#cd /Users/leixingbang/sanxiaProject/ledger-master && mvn clean install -U
#启动命令
# cd /Users/leixingbang/sanxiaProject/ledger-master/ledger-admin/target
#java -jar ledger-admin.jar --spring.profiles.active=prod
