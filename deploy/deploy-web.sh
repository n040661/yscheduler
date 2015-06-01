#!/usr/bin/expect
cd /tmp/yscheduler
set WEB_HOST [lindex $argv 0]
set USER [lindex $argv 1]
set PASSWORD [lindex $argv 2]
set DATE [lindex $argv 3]
set VERSION [lindex $argv 4]
set timeout -1

   #scp
   spawn scp deploy/target/yscheduler-web-${VERSION}.zip $USER@${WEB_HOST}:/dianyi/
   expect "password:" {
      send "${PASSWORD}\r"
   }
   expect "100%"
   #login
   spawn ssh $USER@$WEB_HOST
   expect "(yes/no)?" {
         send "yes\r"
         expect "password:" {
             send "$PASSWORD\r"
         }
   } "password:" {
         send "$PASSWORD\r"
   }
   expect "*~*"
   #stop
   send "jetty.sh stop\r"
   expect "*~*"
   #stop

   #backup,unzip
   send "rm /dianyi/backup/ROOT* -rf\r"
   expect "*~*"
   send "mv /dianyi/webapps/ROOT /dianyi/backup/ROOT.${DATE}\r"
   expect "*~*"
   send "unzip -q /dianyi/yscheduler-web-${VERSION}.zip -d /dianyi/webapps/\r"
   #start
   expect "*~*"
   send "jetty.sh start\r"
   expect "*~*"
   send "exit\r"
   expect "*closed*"
exit