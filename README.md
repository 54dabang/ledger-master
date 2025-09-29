##台账系统
### 打包命令
~~~shel
mvn clean package -Dmaven.test.skip=true
~~~

### 请求token示例
~~~curl
http://localhost:3020/chat/share?shareId=8fymqruveg60zv93drc1n6np&projectId=10&token=eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImxvZ2luX3VzZXJfa2V5IjoiMzlkNWQwMmMtMzliNC00NWRmLWI4NWYtNWQ2ODg2OTlhODI3In0.wRgcV26nr_0YEAfRMrZxkUcaEOjRi8W-M--xdZDabSYJ42GiXbUglj5x00ex4br9HMPoE7VpK5oafHLBQASChg
~~~