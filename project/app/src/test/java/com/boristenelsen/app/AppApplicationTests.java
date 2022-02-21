package com.boristenelsen.app;

import com.boristenelsen.app.api.services.SqlMethodProvider;
import com.boristenelsen.app.database.models.Table;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class AppApplicationTests {

	@Test
	void testSqlMethodProvider() throws JSQLParserException {

	}
}
