# ontology-store-ws
A REST API for the ontology-store plugin.

### Prerequisite
- AWS S3 Account
- Wildfly 17.0.1.Final

## Setting AWS Credentials

AWS credentials are required to access AWS S3.  The AWS credentials are stored in a local file named ***credentials***, in a folder named **.aws** in your home directory.  This is a common practice for storing AWS credentials.  See [Configuration and credential file settings](https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-files.html) for more detail.

The ***credentials*** file:

```text
[default]
aws_access_key_id=
aws_secret_access_key=
```

### Example

Assume the following:

| Attribute             | Value                                    |
|-----------------------|------------------------------------------|
| User Home Directory   | /home/ckent                              |
| AWS Access Key ID     | AKIAIOSFODNN7EXAMPLE                     |
| AWS Secret Access Key | wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY |

The ***/home/ckent/.aws/credentials*** file should look like this:
```text
[default]
aws_access_key_id=AKIAIOSFODNN7EXAMPLE
aws_secret_access_key=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
```

## Setting Application Properties

The application properties are stored in the ***application.properties*** file in the **src/main/resources** directory.
```properties
ontology.dir.download=
ontology.aws.s3.bucket.name=
ontology.aws.s3.key.name=
```
