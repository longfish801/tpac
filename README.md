# tpac

## Overview

Handles documents containing multi-line text.
It is useful when you want to manage several short sentences or scripts into a single file.

This is individual development, for self-learning.  
No support such as troubleshooting, answering inquiries, and so on.

## Features

* Parse, reference, stringify, and merge tpac documents.
  tpac document is a document written in tpac notation.

* tpac notation is an easy notation for writing multi-line text.
  You can write text freely, without inserting indentation or escaping.
* You can use tpac documents as your own DSL.

The name of this library comes from the acronym "Text, Parent And Child".

## Sample Code

Here is a sample tpac document (src/test/resources/sample.tpac).

```
#! thread
#>

#> mail:1
#-from Lucy
Hi everyone.
Any good scripts?
#>

#>> mail:2
#:Reply message for 1
#-from Tom
#-attachment @/attachment:2/script
How about this?
#>

#>> mail:3
#:Reply message for 1
#-from Lucy
#-attachment @/attachment:3/result#
Oh ...
Great job!
#>

#!

#! attachment:2
#>

#> script
#-command
groovy hello.groovy
#-hello
println 'Hello, World!'
println 'Hello, tpac!'
#>

#!

#! attachment:3
#>

#> result
Hello, World!
Hello, tpac!
#>

#!
```

Here is a script that reads the above tpac document, and checks its contents with assert (src/test/groovy/Sample.groovy).

```
import io.github.longfish801.tpac.TpacServer

def server
try {
	server = new TpacServer().soak(new File('src/test/resources/sample.tpac'))
} catch (exc){
	exc.printStackTrace()
}

def thread = server['thread']
assert thread.key == 'thread'
assert thread.lowers['mail:1'].from == 'Lucy'
assert thread.lowers['mail:1'].dflt == [ 'Hi everyone.', 'Any good scripts?' ]
assert thread.lowers['mail:1'].lowers['mail:2'].comments == [ 'Reply message for 1' ]
def mail2 = thread.solvePath('mail:1/mail:2')
assert mail2.attachment.refer().hello == [ "println 'Hello, World!'", "println 'Hello, tpac!'" ]
def mail3 = server.solvePath('/thread/mail:1/mail:3')
assert mail3.from == 'Lucy'
assert mail3.attachment.refer() == [ 'Hello, World!', 'Hello, tpac!' ]
assert server.findAll(/^attachment:\d+$/).collect { it.key } == [ 'attachment:2', 'attachment:3' ]
```

This sample code is executed in the execSample task, see build.gradle.

## Next Step

Please see the [documents](https://longfish801.github.io/maven/tpac/) for more detail.

