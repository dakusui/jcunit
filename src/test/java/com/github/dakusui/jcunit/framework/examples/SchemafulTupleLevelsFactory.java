package com.github.dakusui.jcunit.framework.examples;

import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.factor.FactorField;
import com.github.dakusui.jcunit.core.factor.LevelsFactoryBase;
import com.github.dakusui.jcunit.generators.SchemafulTupleGenerator;
import com.github.dakusui.jcunit.generators.SchemafulTupleGeneratorFactory;

import java.lang.reflect.Field;

public class SchemafulTupleLevelsFactory extends LevelsFactoryBase {
  SchemafulTupleGenerator generator = null;
  private Field targetField;

  @Override
  protected void init(Field targetField, FactorField annotation, Object[] parameters) {
    Utils.checknotnull(targetField);
    Utils.checknotnull(annotation);
    Utils.checknotnull(parameters);
    this.generator = SchemafulTupleGeneratorFactory.INSTANCE.createSchemafulTupleGeneratorForField(targetField);
    this.targetField = targetField;
  }

  @Override
  public int size() {
    return (int) this.generator.size();
  }

  @Override
  public Object get(int index) {
    Object ret = Utils.createNewInstanceUsingNoParameterConstructor(this.targetField.getType());
    Utils.initializeObjectWithSchemafulTuple(ret, this.generator.get(index));
    return ret;
  }
}
