#!/usr/bin/env groovy 

@Grab('org.codehaus.groovy.modules.http-builder:http-builder:0.5.0-RC3')
import groovyx.net.http.RESTClient
import static groovyx.net.http.ContentType.JSON
import org.apache.http.*
import org.apache.http.protocol.*
import org.apache.http.auth.*

def die(msg) {
	System.with {
		err.println(msg)
		exit(1)
	}
}

def membersFile = new File("members")
if (!membersFile.exists()) {
	die("Couldn't find members file, are you in the directory containing this script?")
}
def members = []
membersFile.eachLine {
	def member = it.trim()
	if (member ==~ /^(?!#).*/) { // exclude any line starting with #
		members << member
	}
}
if (!members) {
	die("members file is either empty or all comments")
}

println "GPC Members (from members file):"
members.each {
	println "	- $it"
}
println ""

def passwordFile = new File("password")
if (!passwordFile.exists()) {
	die("Couldn't find password file, put the GitHub gpc user's password in a file called 'password' in the same dir as this script")
}
def password = passwordFile.text.trim()
def username = 'gpc'
def userPassBase64 = "$username:$password".toString().bytes.encodeBase64()

def github = new RESTClient("http://github.com/api/v2/json/").with {
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

def repos = []
println "Getting repository listing…"
github.get(path: "repos/show/$username").with {
	data.repositories.each {
		def repo = it.name
		repos << repo
	}
}
println "	- done (${repos.size()} repos).\n"

repos.each { repo ->
	println "Checking repo '$repo'…"
	def collaborators = []
	github.get(path: "repos/show/$username/$repo/collaborators").with {
		collaborators.addAll(data.collaborators)
	}
	collaborators.remove('gpc')
	
	def toAdd = members.findAll { !(it in collaborators) }
	toAdd.each { user ->
		println "	- adding '$user'"
		github.post(path: "repos/collaborators/$repo/add/$user")
	}
	
	def toRemove = collaborators.findAll { !(it in members) }
	toRemove.each { user ->
		println "	- removing '$user'"
		github.post(path: "repos/collaborators/$repo/remove/$user")
	}

println "	- done.\n"
}

println "All done."