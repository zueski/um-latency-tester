import json, re, sys, requests

logfilename = sys.argv[1]
url = sys.argv[2]

matcher = re.compile("^.result message -> (.*)$")

with open(logfilename, newline='', encoding='utf-8') as logfile:
	line = logfile.readline()
	cnt = 1
	matched = matcher.match(line)
	if matched:
		r = requests.put(url, data=matched.group(1), headers={"Content-Type":"application/json"})
		print("Got response ", r.ok)

# use ls | xargs -n 1 -- curl -XPUT https://vpc-enteprisedictionarydemo-w4aozyeilox77g7fqlfd5pbajy.us-east-1.es.amazonaws.com/enterprisedictionary/_doc/ -H "Content-Type: application/json" --upload-file