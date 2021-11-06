import json
import boto3
from datetime import datetime
import bisect
import re
import hashlib

def lambda_handler(event, context):
    event_json = json.loads(event["body"])
    time_stamp = event_json["time_stamp"]
    time_interval = event_json["time_interval"]
    bucket = event_json["bucket"]
    print(time_stamp, time_interval, bucket)
    pattern = event_json.get("pattern", "([a-c][e-g][0-3]|[A-Z][5-9][f-w]){5,15}")
    items = []
    response = None
    for item in parse_files_in_s3(bucket):
        response = binary_search(bucket, item, time_stamp, time_interval, pattern)
        if response["statusCode"] == 200:
            break
    return response


def parse_files_in_s3(bucket):
    client = boto3.client("s3")
    paginator = client.get_paginator('list_objects_v2')
    page_iterator = paginator.paginate(Bucket=bucket)
    for page in page_iterator:
        if page['KeyCount'] > 0:
            for item in page['Contents']:
                yield item


def binary_search(bucket, item, time_stamp, time_interval, pattern):
    time_stamp = datetime.strptime(time_stamp, "%H:%M:%S")
    time_interval = datetime.strptime(time_interval, "%H:%M:%S")
    time_zero = datetime.strptime("00:00:00", "%H:%M:%S")
    time_range = (str(time_stamp - time_interval), str((time_stamp - time_zero + time_interval).time()))
    client = boto3.client("s3")
    response = client.get_object(Bucket=bucket, Key=item["Key"])
    content = response['Body'].iter_lines()
    timestamps = []
    log_data = []
    for line in content:
        line = line.decode('utf-8')
        line_split = line.split(" ")
        timestamps.append(datetime.strptime(line_split[0].split(".", 1)[0], "%H:%M:%S"))
        log_data.append(line)
    start = bisect.bisect_left(timestamps, datetime.strptime(time_range[0], "%H:%M:%S"))
    end = bisect.bisect_left(timestamps, datetime.strptime(time_range[1], "%H:%M:%S"))
    log_data = log_data[start:end]
    matched = [item["Key"]]
    pattern = re.compile(pattern)
    for msg in log_data:
        msg_list = msg.split(" ")
        msg_log = msg_list[5]
        match_flag = re.search(pattern, msg_log)
        if match_flag:
            matched.append(hashlib.md5(msg_log.encode('utf-8')).hexdigest())
    response = None
    if matched:
        response = {
            "statusCode": 200,
            "headers": {"Content-Type": "application/json"},
            "body": json.dumps(matched)
        }
    else:
        response = {
            "statusCode": 404,
            "headers": {"Content-Type": "application/json"},
            "body": json.dumps([])
        }
    return response
