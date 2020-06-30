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
