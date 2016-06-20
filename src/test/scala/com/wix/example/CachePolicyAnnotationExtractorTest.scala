package com.wix.example

import com.wix.example.CachePolicyTestInterfaces.{SpecificAgeCacheClassTestInterface, TestInterface, maxAge, anotherMaxAge}
import com.wix.example.CachePolicyType.SpecificAge
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope


class CachePolicyAnnotationExtractorTest extends SpecWithJUnit {

  trait ctx extends Scope {
    val cachePolicyAnnotationExtractor: CachePolicyAnnotationExtractor = new SpringAnnotationUtilsCachePolicyAnnotationExtractor
  }


  "CachePolicyAnnotationExtractor" should {

    "return nothing in case a method does not have an annotation" in new ctx {
      val method = classOf[TestInterface].getMethod("methodWithoutAnnotation")

      cachePolicyAnnotationExtractor.cachePolicyFrom(method) must beNone
    }

    "return specific age annotation for method with specific method annotation" in new ctx {
      val method = classOf[TestInterface].getMethod("specificAgeCacheMethod")

      cachePolicyAnnotationExtractor.cachePolicyFrom(method) must beSome( CachePolicyData(SpecificAge, maxAge))
    }

    "return class annotation in case method does not have an annotation" in new ctx {
      val method = classOf[SpecificAgeCacheClassTestInterface].getMethod("methodWithoutAnnotation")

      cachePolicyAnnotationExtractor.cachePolicyFrom(method) must beSome( CachePolicyData(SpecificAge, maxAge))
    }

    "when have annotation on method and class, return the class annotation" in new ctx {
      val method = classOf[SpecificAgeCacheClassTestInterface].getMethod("specificAgeCacheMethod")

      cachePolicyAnnotationExtractor.cachePolicyFrom(method) must beSome( CachePolicyData(SpecificAge, anotherMaxAge))
    }
  }
}

object CachePolicyTestInterfaces {
  trait TestInterface {
    def methodWithoutAnnotation()

    def noCacheMethod()

    @CachePolicy(value = SpecificAge, cacheAgeInSeconds = maxAge)
    def specificAgeCacheMethod()
  }

  @CachePolicy(value = SpecificAge, cacheAgeInSeconds = maxAge)
  trait SpecificAgeCacheClassTestInterface {
    def methodWithoutAnnotation()

    @CachePolicy(value = SpecificAge, cacheAgeInSeconds = anotherMaxAge)
    def specificAgeCacheMethod()
  }

  final val maxAge = 999888
  final val anotherMaxAge = 777555
}