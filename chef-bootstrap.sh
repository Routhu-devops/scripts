#!/bin/bash
set -e

# Since the "official" enroll_chef.sh has the validator PEM data inside, you must authenticate to obtain this.
OS=`uname -s`
if [[ "$OS" == "Linux" ]]; then
  # Add certs
  echo "Installing chef-client"
  yum -y install chef-13.8.5-1.el7.x86_64 ca-bundle --nogpg
  echo "Cleaning up /etc/chef ..."
  rm -rf /etc/chef/
elif [[ "$OS" == "AIX" ]]; then
  mkdir -p /etc/pki/ca-trust/extracted/pem/
  curl -ok /etc/pki/ca-trust/extracted/pem/tls-ca-bundle.pem https://chef server pem files location
  curl --cacert /etc/pki/ca-trust/extracted/pem/tls-ca-bundle.pem https://artifacts URL for wget-1.19.1-1.aix6.1.ppc.rpm > /tmp/wget-1.19.1-1.aix6.1.ppc.rpm
  yum -y localinstall /tmp/wget-1.19.1-1.aix6.1.ppc.rpm
else
  echo "I can't determine what OS this host is running."
fi

echo "Please enter User ID: " >&2
read -s REAL_ID

echo "Please enter password for $REAL_ID: " >&2
read -s AD_PASS

HEADER=`echo -n "$REAL_ID:$AD_PASS" | openssl enc -a`

TMP_DIR=/tmp/chef_install
test -d $TMP_DIR || mkdir -p $TMP_DIR
chmod 700 $TMP_DIR

# Fix /etc/profile and add Chef SSL_CERT_FILE
echo "Fixing /etc/profile and adding SSL_CERT_FILE environment variable ..."
sed '/echo "Authorized sesudo commands for `sewhoami`:"/d' /etc/profile > /etc/profile.sed
cp /etc/profile.sed /etc/profile
sed '/sesudo -list/d' /etc/profile > /etc/profile.sed
cp /etc/profile.sed /etc/profile
rm /etc/profile.sed
grep -q 'export SSL_CERT_FILE=/etc/pki/ca-trust/extracted/pem/tls-ca-bundle.pem' /etc/profile || echo $'\n# For Chef Server connectivity\nexport SSL_CERT_FILE=/etc/pki/ca-trust/extracted/pem/tls-ca-bundle.pem' >> /etc/profile
export SSL_CERT_FILE=/etc/pki/ca-trust/extracted/pem/tls-ca-bundle.pem

# Download package
echo "Downloading enroll_chef.sh script ..."
wget -q --header="Authorization: Basic $HEADER" \
  --ca-certificate=/etc/pki/ca-trust/extracted/pem/tls-ca-bundle.pem \
  -O $TMP_DIR/enroll_chef.sh \
  https://githubURl/enroll_chef.sh

# Do not abort if these commands fail. Instead, store exit code and remove the file w/validator PEM data.
set +e

echo "Executing enroll_chef.sh ..."
chmod +x $TMP_DIR/enroll_chef.sh
$TMP_DIR/enroll_chef.sh
EXIT=$?

echo "Cleaning up ..."
rm -f $TMP_DIR/enroll_chef.sh
source /etc/profile
exit $EXIT
