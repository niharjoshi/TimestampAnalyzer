# Name: Nihar Shailesh Joshi

# Homework 3

## Introduction
In this assignment, we build a Log File Analyzer framework composed of three interlocked components.

First, we deploy a Lambda function on AWS and expose it via a public endpoint using the AWS API Gateway.
This Lambda function analyzes log files stored in an S3 bucket and finds whether any logs within a given timeframe are present in them.

Second, we deploy the LogFileGenerator application on an AWS EC2 instance and make it continuously write logs to an S3 bucket.

Third, we build a gRPC client-server application that communicates with our Lambda function and remotely executes it, returning the md5 hashes of all the matched logs.

The whole deployment process is described in this video: [YouTube](https://youtu.be/ZbXPdKUON9k)

## Prerequisites & Installation

In order to run the algorithms implemented in this assignment, I recommend cloning this repository onto your local machine and running it from the command-line using the interactive build tool **sbt**.

*Note: In order to install sbt, please follow the OS-specific instructions at https://www.scala-sbt.org/1.x/docs/Setup.html.*

To clone the repo use:
```console
git clone https://github.com/niharjoshi/TimestampAnalyzer.git
```

First, sbt downloads project dependencies, generates gRPC classes from protobuf, and compiles.
To do this, use the following command:
```console
sbt clean; sbt compile
```

Next, navigate to the repo and use the following command to run the unit test cases:
```console
sbt test
```

In order to run the gRPC server, use the following command:
```console
sbt "runMain TimestampAnalyzer.TimestampAnalyzerServer"
```

In order to run the gRPC client and provide input parameters, use the following command:
```console
sbt "runMain TimestampAnalyzer.TimestampAnalyzerClient 17:51:52 00:00:01 log-file-generator-data"
```

The first input parameter is the starting timestamp.
The second timestamp is the time delta that produces the time range (timestamp - delta to timestamp + delta).
The third parameter is the S3 bucket from where the logs file(s) must be read.

Our AWS API Gateway endpoint is: https://72v5vwkii3.execute-api.us-east-2.amazonaws.com/prod/analyze

In order to send POST or GET requests to it, use Postman or cURL with the following JSON body as sample input:
```
{
    "time_stamp": "17:51:52",
    "time_interval": "00:00:01",
    "bucket": "log-file-generator-data"
}
```

Example cuRL commands:
```
curl --location --request POST 'https://72v5vwkii3.execute-api.us-east-2.amazonaws.com/prod/analyze' \
--header 'Content-Type: application/json' \
--data-raw '{
    "time_stamp": "17:51:52",
    "time_interval": "00:00:01",
    "bucket": "log-file-generator-data"
}'
```
```
curl --location --request GET 'https://72v5vwkii3.execute-api.us-east-2.amazonaws.com/prod/analyze' \
--header 'Content-Type: application/json' \
--data-raw '{
    "time_stamp": "17:51:52",
    "time_interval": "00:00:01",
    "bucket": "log-file-generator-data"
}'
```

## Architeture & Design + Brief Deployment Steps

The following diagram summarizes the system architecture.

The detailed deployment process is explained in this YouTube video: [YouTube](https://youtu.be/ZbXPdKUON9k)

![Alt text](doc/workflow.jpg?raw=true "System Architecture")

We start by creating an S3 bucket named **log-file-generator-data** to store our log files.
Then, we create an IAM role for EC2 and add the "S3Read and S3Write" permissions to it.
Next, we create an EC2 instance and attach this role to it.

After our EC2 instance has booted up, we SSH into it and clone the LogFileGeneratorDeployment repository onto it: https://github.com/niharjoshi/LogFileGeneratorDeployment.git.
This repo is just a modification of LogFileGenerator with the addition of a function that writes the logs to S3 as they are being produced.

Next, we navigate to the repo and run the generator using *sbt run*.
If we check our S3 bucket, logs will be written to it.

Next, we create an IAM role for Lambda and add the "S3Read and S3Write" permissions to it.
Then, we create a new Lambda function in Python and add the **lambda_function.py** code to it.
Once, the Lambda function is deployed, we need to connect it to an API endpoint.

#### Lambda Function
The Lambda function has been written in Python 3.9.
It uses Binary Search in order to search through the log files in **O(log n)** time.
The Lambda function code can be found here: [Lambda](https://github.com/niharjoshi/TimestampAnalyzer/blob/master/lambda/lambda_function.py)

Next, we navigate to the AWS API Gateway dashboard and create a new endpoint.
Once we generate our link structure, we add a method to our endpoint.
Since the problem statement requires both REST POST and GET support, we add both these methods.
We also connect our methods to our Lambda function and enable Lambda Proxy Integration.

Once we deploy our API Gateway endpoint, we get a URL that we can use to make requests.

We can test this in Postman or using the cURL command as explained in the previous section.

Finally, we have our gRPC application.
In order to run the application, clone this repo and follow the sbt commands in the previous section.

Once our gRPC client connects to our gRPC server, the server invokes the procedure that connects to the API Gateway enpoint, invokes the Lmabda function and received the list of matched logs.

The response received by our client looks like this:
```
[
    "LogFileGenerator_1000.log",
    "916e57cd9953387a3f69418b603832c9",
    "3f9069387fa9a2afc88dc2cbdaca6be3",
    "f08898a310f8e40cf9e8e49e03041ad2",
    "8460cbfc67c0229a874538034694c89f",
    "0163872e0ec720a244baa789dfd48ccd",
    "0df95ba535e5f790b9afbafbcfefdf2a",
    "cbf29bcc2418255ceac963b4e88dcb52",
    "c2dd28bf63c74a596083f5c527cda57f",
    "04199024e30ca0e05f22c6f3b13733d5",
    "842f60b3fc9b55cc1ea55486d5707f5d",
    "9a1333fc6e4e556e1605f78d54fd7221",
    "e58935f2a4bfc9e686fdb7f7aea7505a",
    "bdc3c28d1a46c4b378058ebc09a4e5ee",
    "afd39535432dca40713ac6756267405e",
    "0dc80647688cc229235648d30ca41d8d",
    "ce502131a63b2eea1d69e232f5e75c41",
    "c224723318083dbc005853b65e22584b",
    "9862cdd730ed1092542dc160e775c81a",
    "6ab780d54f3d55fd473a80995b148ebe",
    "50de2d492852f514593fba6b858a6f2c",
    "b61bace39e8ff8e9cd60d70402d30403",
    "42bb52d0466c3b5abb1971345b7d077d",
    "546986f3772efb592534227d5145959f",
    "79773615678fd361145cfd2bdf6c5a53",
    "c229d6f38331029967d1dc8d0c05a9a9",
    "c5ff8947f4119551bc9a0e71ee154f58",
    "cbeab9cccd5dc8ad777134773b1a9dad",
    "f00985d0e8f8e91934c388251992b8e5",
    "6787be47c1d0a7af92365d41d107ddf4",
    "04106edf927d8af51c7f3ad8563733dd",
    "46d966a085d16f8ff862fb04d6681e3a"
]
```

#### Screenshots:

gRPC server
![Alt text](doc/grpc_server.png?raw=true "gRPC server")

gRPC client
![Alt text](doc/grpc_client.png?raw=true "gRPC client")


## Checklist
- [x] All 3 interlocked tasks
- [x] In-depth documentation
- [x] Successful AWS EC2 deployment
- [x] Successful AWS API Gateway + AWS Lambda deployment
- [x] YouTube video
- [x] More than 5 unit tests
- [x] Comments and explanations
- [x] Logging statements
- [x] No hardcoded values
- [x] No var or heap-based variables used
- [x] No for, while or do-while loops used
- [x] Installation instructions in README
