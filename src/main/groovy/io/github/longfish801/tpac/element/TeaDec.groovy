/*
 * TeaDec.groovy
 *
 * Copyright (C) io.github.longfish801 All Rights Reserved.
 */
package io.github.longfish801.tpac.element;

import groovy.util.logging.Slf4j;
import io.github.longfish801.shared.ArgmentChecker;
import io.github.longfish801.tpac.TeaServer;

/**
 * tpac文書の宣言要素特性です。
 * @version 1.0.00 2017/07/07
 * @author io.github.longfish801
 */
@Slf4j('LOG')
trait TeaDec extends TeaHandle {
	/** TeaServer */
	TeaServer server;
	
	/**
	 * 宣言を準備します。<br/>
	 * インスタンス作成後に必ず実行してください。
	 * @param tag タグ
	 * @param name 名前
	 * @param server TeaServer
	 * @return 自インスタンス
	 */
	TeaHandle setup(String tag, String name, TeaServer server) {
		ArgmentChecker.checkNotNull('TeaServer', server);
		this.tag = tag;
		this.name = name;
		this.server = server;
		server << this;
		return this;
	}
	
	/**
	 * タグ、名前、上位ハンドルを格納します（サポート対象外）。
	 * @param tag タグ
	 * @param name 名前
	 * @param upper 上位ハンドル
	 * @return 自インスタンス
	 * @throws UnsupportedOperationException サポートされていないオペレーションです。
	 */
	TeaHandle setup(String tag, String name, TeaHandle upper) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * この宣言の階層を返します。
	 * @return 階層
	 */
	@Override
	int getLevel(){
		return 0;
	}
	
	/**
	 * この宣言のパスを返します。
	 * @return パス
	 */
	@Override
	String getPath(){
		return "${cnstTeaHandle.path.level}${key}" as String;
	}
	
	/**
	 * パスに対応するハンドルを返します。
	 * @param path パス
	 * @return パスに対応するハンドル
	 */
	TeaHandle path(String path){
		ArgmentChecker.checkNotNull('パス', path);
		return (path.startsWith(cnstTeaHandle.path.level))? server.path(path) : super.path(path);
	}
}
