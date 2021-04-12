# Delius Oracle Database
Scripts for building and publishing a dev/test instance of an Oracle 18c XE database, loaded with the Delius PL/SQL code and a set of synthetic test data.

This is intended for use in integration tests where core Delius functionality (e.g. triggers) must be exercised.

## Building Locally
First, agree to the license and download the Oracle RPM file from here: https://www.oracle.com/database/technologies/xe-downloads.html

Then, build the base image (`oracle/database:18.4.0-xe`):
```shell
# 1. Clone the Oracle docker-images repo
git clone https://github.com/oracle/docker-images.git
cd docker-images/OracleDatabase/SingleInstance/dockerfiles

# 2. Copy the Oracle RPM file into place
cp path/to/downloaded/oracle-database-xe-18c-1.0-1.x86_64.rpm 18.4.0

# 3. Remove volume configuration from the Dockerfile
sed -i '/volume/d' Dockerfile.xe

# 4. Build the base image
./buildDockerImage.sh -v 18.4.0 -x
```

Finally, build the Delius image:
```shell
# 1. Download the export file and uplift scripts for the target NDelius release version
#    e.g. for NDelius v4.10.3:
aws s3 cp s3://tf-eu-west-2-hmpps-eng-dev-delius-core-dependencies-s3bucket/dbbackup/ST9_20210317.dmp import.dmp
aws s3 cp --recursive s3://tf-eu-west-2-hmpps-eng-dev-delius-core-dependencies-s3bucket/dependencies/delius-core/NDelius-4.10.3/scripts scripts/delius && dos2unix scripts/delius/*

# 2. Build
docker build -t hmpps/delius-test-db .
```

## Running From ECR
A pre-built image is available from a private Delius ECR repository:
```
895523100917.dkr.ecr.eu-west-2.amazonaws.com/hmpps/delius-test-db
```
Access can be requested in the [#delius_infra_support](https://mojdt.slack.com/archives/CNXK9893K) Slack channel.

An updated image is published each week with the latest Delius physical data model (PDM) - see [buildspec.yml](buildspec.yml) and [build-delius-test-db.tf](https://github.com/ministryofjustice/hmpps-delius-pipelines/blob/master/engineering/builds/build-delius-test-db.tf).


Follow these instructions to pull the image: 
1. Configure the AWS CLI to assume a role with access to ECR in the Delius Engineering account. 
   Below is a sample `~/.aws/credentials` file for assuming the `MoJDevelopers` role with support for MFA.
<pre>
[default]
aws_access_key_id = <b>ACCESS_KEY_ID</b>
aws_secret_access_key = <b>SECRET_ACCESS_KEY_ID</b>
[eng-dev]
source_profile = default
mfa_serial = arn:aws:iam::570551521311:mfa/<b>IAM_USERNAME</b>
role_arn = arn:aws:iam::895523100917:role/MoJDevelopers
</pre>
2. Login to ECR:
```shell
aws ecr get-login-password --profile eng-dev | docker login --username AWS --password-stdin 895523100917.dkr.ecr.eu-west-2.amazonaws.com
```
3. Pull the image and start a container:
```shell
docker run -p 1521:1521 895523100917.dkr.ecr.eu-west-2.amazonaws.com/hmpps/delius-test-db:latest
```

The Oracle Database will be available on local port `1521`, and can be accessed using any of the following accounts :
* `delius_app_schema` (password=`NDelius1`) - for full access to the Delius application tables and code,
* `delius_pool` (password=`NDelius1`) - for restricted access using Oracle VPD,
* `system` (password=`NDAmanager1`) - for system-level access, useful for managing users etc.

The service name should be set to `XEPDB1`.

*Example:*
```shell
# Pull and run the latest database image 
docker run -d -p 1521:1521 895523100917.dkr.ecr.eu-west-2.amazonaws.com/hmpps/delius-test-db:latest 
 
# Connect to the database via Oracle SQL*Plus
docker exec -it <<container-id>> sqlplus delius_app_schema/NDelius1@XEPDB1

# Test the database contains data
> SELECT COUNT(*) FROM OFFENDER;
10838
```
