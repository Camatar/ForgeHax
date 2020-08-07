package dev.fiki.forgehax.main.util.modloader;

import dev.fiki.forgehax.main.util.cmd.flag.EnumFlag;
import dev.fiki.forgehax.main.util.mod.Category;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created on 5/16/2017 by fr1kin
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RegisterMod {
  String value() default "";
  String name() default "";

  String description() default "";

  Category category() default Category.NONE;

  EnumFlag[] flags() default {};

  boolean enabled() default false;
}
