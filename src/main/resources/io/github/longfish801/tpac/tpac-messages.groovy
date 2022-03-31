
// 例外メッセージ
exc {
	invalidpath = 'Invalid path, check syntax. path=%s'
	noSupportScalarString = 'The scalar is no support for stringification. value=%s class=%s'
	cannotSkipLevel = 'Cannot skip level of handle. tag=%s, name=%s, level=%s'
	duplicateMapKeyText = 'Duplicate map key for text. key=%s'
	duplicateMapKey = 'Duplicate map key. key=%s, scalar=%s'
	duplicateHandleKey = 'Duplicate handle key. key=%s, upper=%s'
	noSharpTop = 'No sharp is allowed at top of the text.'
	commentBeforeMap = 'Comments can only come before the map.'
	cannotBlankMapText = 'Cannot specify a blank line for text as a map value, .'
	cannotEmptyListText = 'Cannot specify an empty list as text.'
	invalidDec = 'Invalid declaration, check syntax.'
	invalidHandle = 'Invalid handle beginning line, check syntax.'
	invalidIdKey = 'Invalid identification key, check syntax.'
	parseError = 'Failed to parse tpac document.'
}

// 妥当性検証エラーメッセージ
validate {
	unspecifiedValue = 'No key is specified. It is required. key=%s'
	invalidType = 'Invalid type of key value, check syntax. key=%s type=%s'
}
