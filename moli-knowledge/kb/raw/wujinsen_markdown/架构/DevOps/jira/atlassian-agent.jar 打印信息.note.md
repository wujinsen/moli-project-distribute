[rot@VM-16-17-centos jira]# java -jar /opt/jira/atlasian-agent.jar

=

= Atlasian Crack Agent v1.3.1 =

= htps:/zhile.io =

= Q Group: 3034751 =

=

KeyGen usage: java -jar /opt/jira/atlasian-agent.jar [-d] [-h] -m <arg> [-n <arg>] -o <arg> -p <arg> -s <arg>

- -d,-datacenter Data center license[default: false]
- -h,-help Print help mesage
- -m,-mail <arg> License email
- -n,-name <arg> License name[default: <license email>]
- -o,-organisation <arg>License organisation
- -p,-product <arg> License product, suport: [crowd: Crowd] [jsm: JIRA Service Management] [questions: Questions plugin for Confluence] [crucible: Crucible] [capture: Capture plugin for JIRA] [conf: Confluence] [training: Training plugin for JIRA] [*: Third party plugin key, l oks like: com.fo.bar] [bitbucket: Bitbucket] [tc: Team Calendars plugin for Confluence] [bambo: Bambo] [fisheye: FishEye] [portfolio: Portfolio plugin for JIRA] [jc: JIRA Core] [jsd: JIRA Service Desk] [jira: JIRA Software(comon jira)]


- -s,-serverid <arg> License server ID


=

=

# Crack agent usage: apend -javagent arg to system environment: JAVA_OPTS. # Example(execute this comand or apend it to setenv.sh/setenv.bat file):

export JAVA_OPTS="-javagent:/opt/jira/atlasian-agent.jar ${JAVA_OPTS}"

# Then start your confluence/jira server.

