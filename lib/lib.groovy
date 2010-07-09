@Grab('org.codehaus.groovy.modules.http-builder:http-builder:0.5.0-RC3')
import groovyx.net.http.RESTClient
import static groovyx.net.http.ContentType.JSON
import org.apache.http.*
import org.apache.http.protocol.*
import org.apache.http.auth.*

die = { msg ->
	System.with {
		err.println(msg)
		exit(1)
	}
}

readLine = { prompt = null ->
	if (prompt) {
		println prompt
	}
	def bytes = new ByteArrayOutputStream()
	def c

	c = System.in.read()
	while (c != "\n") {
		bytes.write(c)
		c = System.in.read()
	}
	
	bytes.toString()
}

argOrRead = { i, prompt -> 
	if (args.size() > i) {
		args[i]
	} else {
		readLine(prompt)
	}	
}

confirm = { prompt ->
	def response = readLine("$prompt (enter 'y'):")
	if (response.toUpperCase() != 'Y') {
		die("cancelled")
	}
}

def passwordFile = new File("data/password")
if (!passwordFile.exists()) {
	die("Couldn't find password file, put the GitHub gpc user's password in a file called 'password' in the same dir as this script")
}

password = passwordFile.text.trim()
username = 'gpc'
userPassBase64 = "$username:$password".toString().bytes.encodeBase64()

github = new RESTClient("https://github.com/api/v2/json/").with {
	contentType = JSON
	handler.failure = { resp ->
		die("GitHub API Failure: ${resp.statusLine}")
	}
	client.addResponseInterceptor(
		[process: { HttpResponse response, HttpContext context ->
			response.removeHeaders('Set-Cookie') // httpclient can't parse this, so remove it
		}] as HttpResponseInterceptor
	, 0)
	client.addRequestInterceptor(
		[process: { HttpRequest request, HttpContext context ->
			// using httpbuilders auth mechanism doesn't work, do it manually
			request.setHeader("Authorization", "Basic $userPassBase64")
		}] as HttpRequestInterceptor
	)

	delegate
}

def membersFile = new File("data/members")
if (!membersFile.exists()) {
	die("Couldn't find members file, are you in the directory containing this script?")
}

members = []
membersFile.eachLine {
	def member = it.trim()
	if (member ==~ /^(?!#).*/) { // exclude any line starting with #
		members << member
	}
}
if (!members) {
	die("members file is either empty or all comments")
}

getRepos = { ->
	def repos = []
	github.get(path: "repos/show/$username").with {
		data.repositories.each {
			def repo = it.name
			repos << repo
		}
	}
	repos
}

repoExists = { repo, repos = getRepos() -> 
	repo.toUpperCase() in repos*.toUpperCase()
}