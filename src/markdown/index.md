# tpac

[TOC levels=2-6]

## 概要

　複数行のテキストを含む文書を操作します。
　いくつもの短い文章やスクリプトをひとつのファイルに整理したいときに便利です。

　個人が学習のために開発したものです。
　故障対応や問合せ回答などのサポートはしていません。

## 特徴

* tpac文書を解析、参照、文字列化、マージできます。
  tpac文書は tpac記法で記述された文書です。
* tpac記法は複数行のテキストを記述しやすい記法です。
  インデントの挿入やエスケープなしに、自由にテキストを記述できます。
* tpac文書を独自の DSLとして利用することができます。

　このライブラリの名称は "Text, Parent And Child"の頭文字に由来しています。

## サンプルコード

　以下に tpac文書のサンプルを示します（src/test/resources/sample.tpac）。

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

　上記の tpac文書を読みこんで、assertで内容を確認するスクリプトです（src/test/groovy/Sample.groovy）。

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

　このサンプルコードは build.gradle内の execSampleタスクで実行しています。

## GitHubリポジトリ

* [tpac](https://github.com/longfish801/tpac)

## ドキュメント

* [Groovydoc](groovydoc/)
* [tpac記法](notation.html)
* [DSLの利用](dsl.html)

## Mavenリポジトリ

　本ライブラリの JARファイルを [GitHub上の Mavenリポジトリ](https://github.com/longfish801/maven)で公開しています。
　build.gradleの記述例を以下に示します。

```
repositories {
	mavenCentral()
	maven { url 'https://longfish801.github.io/maven/' }
}

dependencies {
	implementation group: 'io.github.longfish801', name: 'tpac', version: '0.3.00'
}
```

## 改版履歴

0.3.01
: solvePathメソッドで自ハンドルと一致した場合の分岐は不要なため削除しました。

0.3.02
: TeaHandleにgetDfltメソッドを追加しました。

0.3.03
: パスのアンカーを省略したときはデフォルトキーとみなすよう修正しました。

0.3.04
: ハンドルの名前省略時のデフォルト値を空文字から半角アンダーバーに変更しました。

0.3.05
: 名前が省略された子ハンドルは半角アンダーバーをキーとするよう修正しました。

0.3.06
: キーの妥当性検査や初期化のためのメソッドを見直しました。

0.3.07
: データ型がnullのときも検証できるよう修正しました。

0.3.08
: デフォルト値が未設定のときでもキーが設定される不具合を改修しました。

0.3.09
: スカラー値に改行コードが含まれていても出力時にエスケープされない不具合を改修しました。
: ハンドルの識別キーに重複があってもエラーとならない不具合を改修しました。

0.3.10
: ドキュメントはmavenリポジトリに出力するよう修正しました。
