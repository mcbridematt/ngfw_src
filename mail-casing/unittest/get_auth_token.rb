## A rush script for retrieving an authentication token for an email address.

## (Wherever the rush shell is)
## To run use:  QUARANTINE_ACCOUNT="foo@foo.com" ./dist/usr/bin/rush ./mail-casing/unittest/get_auth_token.rb 

require 'java'
nm = RUSH.uvm.nodeManager()
tid = nm.nodeInstances( 'untangle-casing-mail' ).first
mail = nm.nodeContext( tid ).node

account = ENV["QUARANTINE_ACCOUNT"]
puts "Retrieving authentication token for '#{account}'"
token = mail.createAuthToken(account)
puts "The authentication token for '#{account}' is '#{token}' URLEncoded: '#{java.net.URLEncoder.encode( token )}'"
