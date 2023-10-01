package com.example.custom.cache.aop;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.codec.digest.DigestUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.stereotype.Component;

import com.example.custom.cache.annotation.MyCache;
import com.example.custom.cache.entity.CacheEntity;
import com.example.custom.cache.repository.MyCacheRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@Aspect
@Component
public class MyCacheAop{

    @Autowired
    MyCacheRepository myCacheRepository;

  @Around("@annotation(com.example.custom.cache.annotation.MyCache)")
  public Object cache(final ProceedingJoinPoint pjp) throws Throwable {
    final List<String> paramValueList = new ArrayList<>();
    Arrays.stream(pjp.getArgs()).forEach(param -> this.getPropertiesValue(param, paramValueList));

    final String cacheKey = this.getCacheKey(paramValueList);

    final CacheEntity cacheEntity = this.getCacheEntity(cacheKey);

    if (cacheEntity != null) {
      final String response = cacheEntity.getCacheValue();
      if (response != null) {
        final ObjectMapper objectMapper = new ObjectMapper();
        final Class<?> klass = Class.forName(cacheEntity.getObjType());
        return objectMapper.readValue(response, klass);
      }
    }

    final Object returnValue = pjp.proceed();

    final String jsonString = new ObjectMapper().writeValueAsString(returnValue);

    final MethodSignature signature = (MethodSignature) pjp.getSignature();
    final Method method = signature.getMethod();
    final MyCache myCache = method.getAnnotation(MyCache.class);

    this.myCacheRepository.save(new CacheEntity(cacheKey, jsonString, returnValue.getClass().getName(),
        LocalDateTime.now().plusMinutes(myCache.evictTime())));

    return returnValue;
  }

  private CacheEntity getCacheEntity(final String cacheKey) {
    return this.myCacheRepository.findById(cacheKey).orElse(null);
  }

  private String getCacheKey(final List<String> paramValueList) {
    final String concatedValues = paramValueList.stream().map(Object::toString).collect(Collectors.joining(","));
    return DigestUtils.sha256Hex(concatedValues);
  }

  private List<String> getPropertiesValue(final Object paramObj, final List<String> paramValueList) {
    if (paramObj instanceof String || paramObj instanceof Number) {
      paramValueList.add(String.valueOf(paramObj));
      return paramValueList;
    } else if (paramObj instanceof Collection) {
      for (final Object objParam : (Collection<?>) paramObj) {
        this.getPropertiesValue(objParam, paramValueList);
      }
    }

    final BeanMap beanMap = BeanMap.create(paramObj);
    final PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
    beanMap.keySet().stream().sorted().forEach(objAttributeName -> {
      try {
        final String attributeName = String.valueOf(objAttributeName);
        final String atrributeTypeClassName = String.valueOf(propertyUtilsBean.getPropertyType(paramObj, attributeName));
        if (atrributeTypeClassName.contains("java.lang.")) {
          paramValueList.add(String.valueOf(propertyUtilsBean.getProperty(paramObj, attributeName)));
        } else if (atrributeTypeClassName.contains("java.util.Collection")) {
          try {
            if (propertyUtilsBean.getProperty(paramObj, attributeName) != null) {
              for (final Object obj : (Collection<?>) propertyUtilsBean.getProperty(paramObj, attributeName)) {
                for (final Field field : obj.getClass().getDeclaredFields()) {
                  final String atrributeTypeName = field.getGenericType().getTypeName();
                  if (atrributeTypeName.contains("java.lang.Integer") || atrributeTypeName.contains("java.lang.String")) {
                    paramValueList.add(String.valueOf(obj));
                  } else if (atrributeTypeName.contains("java.util.Collection")) {
                    // Do nothing, we are handling it in another place
                  } else if (atrributeTypeName.contains("")) {
                    this.getPropertiesValue(obj, paramValueList);
                  }
                }
              }
            }
          } catch (final Exception e) {
            e.printStackTrace();
          }
        } else if (atrributeTypeClassName.contains("com.example.custom.cache")) {
          this.getPropertiesValue(propertyUtilsBean.getProperty(paramObj, attributeName), paramValueList);
        } else if (attributeName.equals("class") && paramObj != null && !paramObj.toString().isEmpty()) {
          paramValueList.add(paramObj.toString().substring(0,
              paramObj.toString().contains("@") ? paramObj.toString().indexOf("@") : paramObj.toString().length()));
        }
      } catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
        e.printStackTrace();
      }
    });
    return paramValueList;
  }

}