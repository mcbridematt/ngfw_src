import os
import re

from untangle_node_idps.snort_rule import SnortRule

class SnortRules:
    #
    # Process a set of snort rules such as downloaded rules.
    #
    var_regex = re.compile(r'^\$(.+)')
    category_regex = re.compile(r'^# \-+ Begin (.+) Rules Category')
    
    def __init__( self, nodeId = 0, path = "", file_name = "" ):
        self.nodeId = nodeId
        self.path = path
        self.file_name = self.path + "/";
        if file_name != "":
            self.file_name = self.file_name + file_name
        else:
            self.file_name = self.file_name + "node_" + self.nodeId + ".rules"
        
        self.rules = {}
        self.variables = []

    def set_path(self, path = "", file_name = "" ):        
        self.path = path
        self.file_name = self.path + "/";
        if file_name != "":
            self.file_name = self.file_name + file_name
        else:
            self.file_name = self.file_name + "node_" + self.nodeId + ".rules"
        
    def load(self, path = False ):

        if path == True:
            for file_name in os.listdir( self.path ):
                name, extension = os.path.splitext( file_name )
                if extension != ".rules":
                    continue
                self.load_file( self.path + "/" + file_name )
        else:
            self.load_file( self.file_name )
            
    def load_file( self, file_name ):
        
        # Category based on "major" file name separator. 
        # e.g., web-cgi = web
        path, name = os.path.split( file_name )
        name, ext = os.path.splitext( name )
        category = name

        # ? Special handling for "deleted"?
        
        rule_count = 0
        rules_file = open( file_name )
        for line in rules_file:
            # Alternate category match from pulledpork output
            match_category = re.search( SnortRules.category_regex, line )
            if match_category:
                category = match_category.group(1)
            else:            
                match_rule = re.search( SnortRule.text_regex, line )
                if match_rule:
                    self.addRule( SnortRule( match_rule, category ) )
                    rule_count = rule_count + 1
        rules_file.close()
            
    def check_write_rule( self, rule, classtypes, categories, msgs ):
        if len(classtypes) == 0 or rule.options["classtype"] in classtypes:
            classtype_match = True
        else:
            classtype_match = False
        
        if len(categories) == 0 or rule.category in categories:
            category_match = True
        else:
            category_match = False

        if len(msgs) == 0:
            msgs_match = True
        else:
            msgs_match = False
        
        for msg_substring in msgs:
            if rule.options["msg"].lower().find( msg_substring.lower() ) != -1:
                msgs_match = True
                break
        
        return classtype_match and category_match and msgs_match
        
    def save(self, classtypes = [], categories = [], msgs = [] ):
        temp_file_name = self.file_name + ".tmp"
        rules_file = open( temp_file_name, "w" )
        category = "undefined"
        # ? order by category
        for rule in self.rules.values():
            if self.check_write_rule( rule, classtypes, categories, msgs ) == False:
                continue

            if rule.category != category:
                category = rule.category
                rules_file.write( "\n\n# ---- Begin " + category +" Rules Category ----#" + "\n\n")
                
            if rule.get_enabled() == True:
                rules_file.write( rule.build() + "\n" );
        rules_file.close()
        
        if os.path.isfile( self.file_name ):
            os.remove( self.file_name )
        os.rename( temp_file_name, self.file_name )

    def addRule( self, rule ):
        #
        # Add a new rule to the list and search for variables.
        #
        self.rules[rule.options["sid"]] = rule
        for property, value in vars(rule).iteritems():
            if isinstance( value, str ) == False:
                continue
            match_variable = re.search( SnortRules.var_regex, value )
            if match_variable:
                if self.variables.count( match_variable.group( 1 ) ) == 0:
                    self.variables.append( match_variable.group( 1 ) )
        for key in rule.options.keys():
            value = rule.options[key]
            if isinstance( value, str ) == False:
                continue
            match_variable = re.search( SnortRules.var_regex, value )
            if match_variable:
                if self.variables.count( match_variable.group( 1 ) ) == 0:
                    self.variables.append( match_variable.group( 1 ) )
                    
    def get_rules(self):
        return self.rules
    
    def get_variables(self):
        return self.variables

    def get_file_name( self ):
        return self.file_name
