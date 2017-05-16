<%@ page language="java" pageEncoding="utf-8"%>
<%
    String path = request.getContextPath();
    String basePath = request.getScheme() + "://"
            + request.getServerName() + ":" + request.getServerPort()
            + path + "/";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <META HTTP-EQUIV="Cache-Control"
          CONTENT="no-cache,no-store, must-revalidate">
    <META HTTP-EQUIV="pragma" CONTENT="no-cache">
    <META HTTP-EQUIV="expires" CONTENT="0">
    <title>文件系统扫描</title>
    <script type="text/javascript" language="javascript"  src="<%=basePath%>js/jquery-1.9.1.min.js">
    </script>
    <script type="text/javascript" language="javascript">
        $(function(){
            startScan1();
            startScan2();
            startScan3();
            startScan4();
            startScan5();
            startScan6();
        });
        function startScan1() {
            $.post("<%=basePath%>toClient.do",{},function () {

            })
        }
        function startScan2() {
            $.post("<%=basePath%>toClient1.do",{},function () {

            })
        }
        function startScan3() {
            $.post("<%=basePath%>toClient2.do",{},function () {

            })
        }
        function startScan4() {
            $.post("<%=basePath%>toClient3.do",{},function () {

            })
        }
        function startScan5() {
            $.post("<%=basePath%>toClient4.do",{},function () {

            })
        }
        function startScan6() {
            $.post("<%=basePath%>toClientThread.do",{},function () {

            })
        }

    </script>
</head>
<body>

<h2>Hello World!</h2>
<input type="button" value="开始扫描文件系统1" onclick="startScan1();">
<input type="button" value="开始扫描文件系统2" onclick="startScan2();">
<input type="button" value="开始扫描文件系统3" onclick="startScan3();">
<input type="button" value="开始扫描文件系统4" onclick="startScan4();">
<input type="button" value="开始扫描文件系统5" onclick="startScan5();">
<input type="button" value="开始扫描文件系统6" onclick="startScan6();">
</body>
</html>
