package br.ufrgs.rmpestano.intrabundle.util;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

/**
 * Created by rmpestano on 8/17/14.
 */
public class BeanManagerController {

    public static <T> T getBeanByType(BeanManager bm, Class<T> type) {
        Bean bean = bm.getBeans(type).iterator().next();
        CreationalContext ctx = bm.createCreationalContext(bean); // could be inlined below
        T o = (T) bm.getReference(bean, type, ctx); // could be inlined with return
        return o;
    }
}
