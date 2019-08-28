import json, re, sys, requests

logfilename = sys.argv[1]
url = sys.argv[2]
print("loading {} to {}".format(logfilename, url))

matcher = re.compile("^.*result message -> (.*)$")

with open(logfilename, newline='', encoding='utf-8') as logfile:
	line = logfile.readline()
	lines = 0
	sent = 0
	while line:
		lines = lines + 1
		matched = matcher.match(line)
		if matched:
			record = json.loads(matched.group(1))
			sendurl = "{}/{}/_doc/{}/".format(url, record['TestName'], record['Sequence'])
			r = requests.put(sendurl, data=json.dumps(record), headers={"Content-Type":"application/json"})
			#print("Got response ", r.ok)
			sent = sent + 1
		line = logfile.readline()
	print("processed {} lines and sent {} records".format(lines, sent))

# use ls | xargs -n 1 -- curl -XPUT https://vpc-enteprisedictionarydemo-w4aozyeilox77g7fqlfd5pbajy.us-east-1.es.amazonaws.com/enterprisedictionary/_doc/ -H "Content-Type: application/json" --upload-file