1.5 
* When encountering a MIX,MODS,ALTO,EDITION or FILM datastream, change the mimetype to text/xml

1.4
* Use newest version of item event framework. No functional changes for this module.
* Configuration has been extended and changed and example config has been updated. Please update your configuration files.

1.3
* Retry talking to Fedora a number of times

1.2
* Update pollAndWork.sh to invoke the proper class
* Update to newspaper parent 1.2
* Update to version 1.10 of batch event framework
* Limit the number of allowed threads to prevent exhausting available threads
* Make batch enrichment produce valid objects according to content model

1.1 
* Moved rdf-manipulator code to newspaper-prompt-doms-ingester and updated dependencies accordingly as side-effect of fix to NO-210

1.0
* Initial release
