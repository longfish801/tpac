# DSLの利用

[TOC levels=2-6]

## 概要

　本ライブラリは独自の DSLの利用に役立てることができます。

　ハンドル、宣言の実装クラスを独自のクラスに置き換えることができます。
　tpac文書の解析時に、それら独自クラスのインスタンスを作成することができます。

## 詳細

　ハンドルは TpacHandleクラス、宣言は TpacDecクラスで実装しています。
　TpacHandleクラス、TpacDecクラスにはフィールドやメソッドがありません。それぞれTeaHandle特性、TeaDec特性を実装しています。
　独自のクラスに TeaHandle特性、TeaDec特性を実装するだけで、それらは TpacHandleクラス、TpacDecクラスと同等のクラスとなります。

　tpac文書は TpacServerクラスが保持します。TpacPartyクラスで tpac文書の文字列を解析し、TpacMakerクラスでハンドルや宣言のインスタンスを生成します。
　こちらも同じように各クラスはほとんど処理がありません。処理は特性のほうにコーディングしています。
　このため、たとえば TeaMaker特性を実装し、ハンドルや宣言のインスタンスを生成するメソッドをオーバーライドすることで、独自のクラスをハンドルや宣言のインスタンスとして返すことができます。
　ある tpac文書のすべてのハンドルや宣言を独自クラスに置き換える必要はありません。基本的には TpacHandleクラス、TpacDecクラスを用い、必要な箇所だけ独自クラスに置き換えることができます。

| 実装対象 | 実装クラス | 特性      |
| -----    | -----      | -----     |
| ハンドル | TpacHandle | TeaHandle |
| 宣言     | TpacDec    | TeaDec    |
| サーバ   | TpacServer | TeaServer |
| 解析器   | TpacParty  | TeaParty  |
| 生成器   | TpacMaker  | TeaMaker  |

　これらのクラスは io.github.longfish801.tpacパッケージに、特性は io.github.longfish801.tpac.teaパッケージに格納されています。

## サンプルコード

　以下に DSLを実現したサンプルスクリプトを示します（src/test/groovy/SampleDsl.groovy）。
　タグ名「mail」のハンドルを独自の Mailクラスに置き換えています。

```
import io.github.longfish801.tpac.*
import io.github.longfish801.tpac.tea.*

def attachment2 = new TpacDec(tag: 'attachment', name: '2')
def scriptHandle = new TpacHandle(tag: 'script')
scriptHandle.command = [ 'groovy hello.groovy' ]
scriptHandle.hello = [ "println 'Hello, World!'", "println 'Hello, tpac!'" ]
attachment2 << scriptHandle

def attachment3 = new TpacDec(tag: 'attachment', name: '3')
def resultHandle = new TpacHandle(tag: 'result')
resultHandle.dflt = [ 'Hello, World!', 'Hello, tpac!' ]
attachment3 << resultHandle

def mail2 = new Mail('2')
mail2.from = 'Tom'
mail2.appendMessage('How about this?')
mail2.attache(attachment2.path + '/script')

def mail3 = new Mail('3')
mail3.from = 'Lucy'
mail3.appendMessage('Oh ...')
mail3.appendMessage('Great job!')
mail3.attache(attachment3.path + '/result#')

String script = '''\
	#! thread
	#> mail:1
	#-from Lucy
	Hi everyone.
	Any good scripts?
	'''.stripIndent()

def server = new MailServer().soak(script)
def mail1 = server.solve('/thread/mail:1')
assert mail1 instanceof Mail
mail1.reply(mail2)
mail1.reply(mail3)
server << attachment2
server << attachment3

StringWriter writer = new StringWriter()
server.decs.each { it.value.write(writer) }
assert writer.toString().normalize() == new File('src/test/resources/sample.tpac').text

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
		if (getAt('dflt') == null) throw new TpacSemanticException('Message must be specified')
	}
	
	void appendMessage(String line){
		if (this.dflt == null) this.dflt = []
		this.dflt << line
	}
	
	void reply(Mail mail){
		mail.comments << "Reply message for ${name}"
		this << mail
	}
	
	void attache(String path){
		setAt('attachment', TpacRefer.newInstance(handle, path))
	}
}
```

　このサンプルコードは build.gradle内の execSampleDslタスクで実行しています。
