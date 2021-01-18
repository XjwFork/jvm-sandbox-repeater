package com.alibaba.jvm.sandbox.repeater.aide.compare.comparator;

import com.alibaba.jvm.sandbox.repeater.aide.compare.DateTransUtils;
import com.alibaba.jvm.sandbox.repeater.aide.compare.IntegratedComparator;
import com.alibaba.jvm.sandbox.repeater.aide.compare.Difference;
import com.alibaba.jvm.sandbox.repeater.aide.compare.path.Path;
import org.kohsuke.MetaInfServices;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import com.alibaba.jvm.sandbox.repeater.aide.compare.LogUtil;

import static com.alibaba.jvm.sandbox.repeater.aide.compare.TypeUtils.*;

/**
 * {@link SimpleComparator}
 * <p>
 * can compare basic type use '==' or java.util/java.lang/java.math/java.time use 'equals'
 *
 * @author zhaoyb1990
 */
@MetaInfServices(Comparator.class)
public class SimpleComparator implements Comparator {

    @Override
    public int order() {
        return 10000;
    }

    @Override
    public boolean accept(final Object left, final Object right) {
        if (left == right) {
            return true;
        }
        if (left == null || right == null) {
            return true;
        }
        Class<?> lCs = left.getClass();
        Class<?> rCs = right.getClass();
        if (isArray(lCs, rCs) || isCollection(lCs, rCs) || isMap(lCs, rCs)) {
            return false;
        }
        // type different
        if (lCs != rCs) {
            return true;
        }
        // basic type or java.lang or java.math or java.time or java.util
        return isBasicType(lCs, rCs) || isBothJavaLang(lCs, rCs)
                || isBothJavaMath(lCs, rCs) || isBothJavaTime(lCs, rCs) || isBothJavaUtil(lCs, rCs);
    }

    @Override
    public void compare(Object left, Object right, List<Path> paths, IntegratedComparator comparator) {
        // default use '==' to compare
        if (left == right) {
            return;
        }
        // null check
        if (left == null || right == null) {
            comparator.addDifference(left, right, Difference.Type.FILED_DIFF, paths);
            LogUtil.info("field different-Simple 1 :left is null || right is null");
            return;
        }
        // 如果left,right都是数字，统一转Double进行比对，避免因反序列化导致的类型不一致
        if(left.getClass().getSuperclass() == java.lang.Number.class && right.getClass().getSuperclass() == java.lang.Number.class){
            left = new Double(left.toString());
            right = new Double(right.toString());
        }

        Class<?> lCs = left.getClass();
        Class<?> rCs = right.getClass();

        // 如果left,right有一方是Date,另一方是String，则统一转String比对
        if(lCs == Date.class || rCs == Date.class){

            try {
                if(lCs == String.class){
                    LogUtil.info("class different-Simple : date transfer1");
                    left = DateTransUtils.stringToDate(left.toString());
                }
                if(rCs == String.class){
                    LogUtil.info("class different-Simple : date transfer2");
                    right = DateTransUtils.stringToDate(right.toString());;
                }
            } catch (ParseException e) {
                LogUtil.info("class different-Simple : String转Date异常");
            }

            LogUtil.info("class different-Simple-date :left-actual={},right-expect={}",left,right);
            if(left.equals(right)){
                return;
            }
        }

        if (lCs != rCs) {
            comparator.addDifference(left, right, Difference.Type.TYPE_DIFF, paths);
            LogUtil.info("class different-Simple :left-actual={},right-expect={}",lCs,rCs);
            return;
        }

        if(left.equals(right)){
            return;
        }
        // basic type using == to compare
        if (isBasicType(lCs, rCs)) {
            comparator.addDifference(left, right, Difference.Type.FILED_DIFF, paths);
            LogUtil.info("field different-Simple 2 :left ={}|right={}",left,right);
            return;
        }
        // use equals to compare
        if (!left.equals(right)) {
            comparator.addDifference(left, right, Difference.Type.FILED_DIFF, paths);
            LogUtil.info("field different-Simple 3 :left ={}|right={}",left,right);
        }
    }


    @Override
    public boolean support(CompareMode compareMode) {
        return true;
    }
}
