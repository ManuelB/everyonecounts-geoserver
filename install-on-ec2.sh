# Amazon Linux 2 AMI (HVM), SSD Volume Type - ami-0ec1ba09723e5bfac
# t2.micro
# Open ports 22,80,443,9990
sudo dd if=/dev/zero of=/swapfile bs=128M count=16
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
sudo swapon -s
sudo sh -c 'echo "/swapfile swap swap defaults 0 0" >> /etc/fstab'
curl -O https://download.java.net/java/GA/jdk12.0.1/69cfe15208a647278a19ef0990eea691/12/GPL/openjdk-12.0.1_linux-x64_bin.tar.gz
tar xvf openjdk-12.0.1_linux-x64_bin.tar.gz
sudo mv jdk-12.0.1 /opt/
cat <<EOF | sudo tee /etc/profile.d/jdk12.sh
export JAVA_HOME=/opt/jdk-12.0.1
export PATH=\$PATH:\$JAVA_HOME/bin
EOF
source /etc/profile.d/jdk12.sh
sudo wget http://repos.fedorapeople.org/repos/dchen/apache-maven/epel-apache-maven.repo -O /etc/yum.repos.d/epel-apache-maven.repo
sudo sed -i s/\$releasever/6/g /etc/yum.repos.d/epel-apache-maven.repo
# Maven installieren
sudo yum install -y apache-maven
export MAVEN_OPTS=-Xmx1024m
sudo yum install -q git
wget https://download.jboss.org/wildfly/19.0.0.Final/wildfly-19.0.0.Final.zip
sudo unzip wildfly-19.0.0.Final.zip -d /opt/
sudo ln -s /opt/wildfly-19.0.0.Final /opt/wildfly
sudo cp /opt/wildfly/docs/contrib/scripts/init.d/wildfly.conf /etc/default/wildfly
sudo cp /opt/wildfly/docs/contrib/scripts/init.d/wildfly-init-redhat.sh /etc/init.d/wildfly
sudo perl -pi -w -e 's/# JBOSS_USER=wildfly/JBOSS_USER=root/g' /etc/default/wildfly
sudo perl -pi -w -e 's/#JAVA_HOME="\/opt\/java\/jdk"/JAVA_HOME="\/opt\/jdk-12.0.1"/g' /opt/wildfly/bin/standalone.conf
sudo perl -pi -w -e 's/-XX:MetaspaceSize=96M -XX:MaxMetaspaceSize=256m//g' /opt/wildfly/bin/standalone.conf
sudo perl -pi -w -e 's/-Xmx512m/-Xmx768m/g' /opt/wildfly/bin/standalone.conf
sudo perl -pi -w -e 's/# JBOSS_OPTS="--admin-only -b 127.0.0.1"/JBOSS_OPTS="-b=0.0.0.0 -bmanagement=0.0.0.0 -Djboss.http.port=80 -Djboss.https.port=443"/g' /etc/default/wildfly
sudo service wildfly start
git clone https://github.com/ManuelB/everyonecounts-geoserver.git
cd everyonecounts-geoserver/
mvn install
mkdir -p /home/geoserver/data_dir
sudo cp geoserver/target/geoserver.war /opt/wildfly/standalone/deployments
sudo mv /home/geoserver/data_dir/security/masterpw.info /root/
cd ..
sudo su
cd /opt/wildfly/bin/
./add-user.sh
# add user
./jboss-cli.sh --connect
# security enable-ssl-http-server --interactive --lets-encrypt
