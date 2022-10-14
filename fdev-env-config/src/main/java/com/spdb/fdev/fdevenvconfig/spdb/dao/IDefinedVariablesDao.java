package com.spdb.fdev.fdevenvconfig.spdb.dao;

import com.spdb.fdev.fdevenvconfig.spdb.entity.DefinedVariables;

public interface IDefinedVariablesDao {
    DefinedVariables save(DefinedVariables definedVariables);

    DefinedVariables queryDefinedVariables(String id);
}
