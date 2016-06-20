package com.wix.example

import java.lang.reflect.Method

import org.springframework.core.annotation.AnnotationUtils

trait CachePolicyManager {
  def cachePolicyHeadersFor(method: Method): Seq[(String, String)]
}

class AnnotationBasedCachePolicyManager(cachePolicyAnnotationExtractor: CachePolicyAnnotationExtractor)
  extends CachePolicyManager {
  def cachePolicyHeadersFor(method: Method) =
    cachePolicyAnnotationExtractor.cachePolicyFrom(method) match {
      case Some(CachePolicyData(_, maxAge)) if maxAge <= 0 => throw new IllegalArgumentException
      case Some(CachePolicyData(_, maxAge)) => Seq("Cache-Control" -> s"max-age=$maxAge")
      case _ => Seq("Cache-Control" -> "no-cache",
                    "Pragma" -> "no-cache")

    }
}

trait CachePolicyAnnotationExtractor {
  def cachePolicyFrom(method: Method): Option[CachePolicyData]
}

class SpringAnnotationUtilsCachePolicyAnnotationExtractor extends CachePolicyAnnotationExtractor {
  def cachePolicyFrom(method: Method) =
    Option( AnnotationUtils.getAnnotation(method, classOf[CachePolicy]) )
      .orElse( Option( AnnotationUtils.getAnnotation(method.getDeclaringClass, classOf[CachePolicy]) ) )
        .map( a => CachePolicyData(a.value, a.cacheAgeInSeconds))

}

case class CachePolicyData(cachePolicyType: CachePolicyType, maxAge: Int)
