package com.skytonia.SkyCore.database;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Chris Brown (OhBlihv) on 25/09/2016.
 */
public interface ResultAction
{
	
	void processResults(ResultSet resultSet) throws SQLException;
	
}
