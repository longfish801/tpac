import io.github.longfish801.tpac.*
import io.github.longfish801.tpac.tea.*

def attachment2 = new TpacDec(tag: 'attachment', name: '2')
def scriptHandle = new TpacHandle(tag: 'script')
scriptHandle.command = [ 'groovy hello.groovy' ]
scriptHandle.hello = [ "println 'Hello, World!'", "println 'Hello, tpac!'" ]
attachment2 << scriptHandle

def attachment3 = new TpacDec(tag: 'attachment', name: '3')
def resultHandle = new TpacHandle(tag: 'result')
resultHandle._ = [ 'Hello, World!', 'Hello, tpac!' ]
attachment3 << resultHandle

def mail2 = new Mail('2')
mail2.from = 'Tom'
mail2.appendMessage('How about this?')
mail2.attache(attachment2.path + '/script')

def mail3 = new Mail('3')
mail3.from = 'Lucy'
mail3.appendMessage('Oh ...')
mail3.appendMessage('Great job!')
mail3.attache(attachment3.path + '/result#_')

String script = '''\
	#! thread
	#> mail:1
	#-from Lucy
	Hi everyone.
	Any good scripts?
	'''.stripIndent()

def server = new MailServer().soak(script)
def mail1 = server.solvePath('/thread/mail:1')
assert mail1 instanceof Mail
mail1.reply(mail2)
mail1.reply(mail3)
server << attachment2
server << attachment3

StringWriter writer = new StringWriter()
server.decs.each { it.value.write(writer) }
assert writer.toString() == new File('src/test/resources/sample.tpac').text

class MailServer implements TeaServer {
	@Override
	TeaMaker newMaker(String tag){
		if (tag == 'thread') return new MailMaker()
		return TeaServer.super.newMaker(tag)
	}
}

class MailMaker implements TeaMaker {
	@Override
	TeaHandle newTeaHandle(String tag, String name, TeaHandle upper){
		if (tag == 'mail') return new Mail()
		throw new TpacSemanticException("Not allowed tag: tag=${tag}")
	}
}

class Mail implements TeaHandle {
	Mail(String name){
		this.tag = 'mail'
		this.name = name
	}
	
	@Override
	void validate(){
		if (getAt('from') == null) throw new TpacSemanticException('Key "from" must be specified')
		if (getAt('_') == null) throw new TpacSemanticException('Message must be specified')
	}
	
	void appendMessage(String line){
		if (this._ == null) this._ = []
		this._ << line
	}
	
	void reply(Mail mail){
		mail.comments << "Reply message for ${name}"
		this << mail
	}
	
	void attache(String path){
		setAt('attachment', TpacRefer.newInstance(handle, path))
	}
}
