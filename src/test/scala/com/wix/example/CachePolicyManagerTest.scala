package com.wix.example

import com.wix.example.CachePolicyTestInterfaces.TestInterface
import org.specs2.matcher.ThrownExpectations
import org.specs2.mock.Mockito
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope

import scala.util.Random


class CachePolicyManagerTest extends SpecWithJUnit {

  trait ctx extends Scope with Mockito with ThrownExpectations {
    val method = classOf[TestInterface].getMethod("methodWithoutAnnotation")
    val maxAge = Random.nextInt(999)

    val cachePolicyAnnotationExtractor = mock[CachePolicyAnnotationExtractor]
    val cachePolicyManager: CachePolicyManager = new AnnotationBasedCachePolicyManager(cachePolicyAnnotationExtractor)
  }


  "CachePolicyManager" should {

    "given no annotation return default cache headers" in new ctx {
      cachePolicyAnnotationExtractor.cachePolicyFrom(method) returns None

      cachePolicyManager.cachePolicyHeadersFor(method) must containTheSameElementsAs(Seq("Cache-Control" -> "no-cache",
                                                                                         "Pragma" -> "no-cache"))
    }

    "given specific age annotation, return cache headers with time" in new ctx {
      cachePolicyAnnotationExtractor.cachePolicyFrom(method) returns Some(CachePolicyData(CachePolicyType.SpecificAge, maxAge))

      cachePolicyManager.cachePolicyHeadersFor(method) must
        containTheSameElementsAs(Seq("Cache-Control" -> s"max-age=$maxAge"))
    }

    "for invalid max age throw an illegal argument exception" in new ctx {
      cachePolicyAnnotationExtractor.cachePolicyFrom(method) returns Some(CachePolicyData(CachePolicyType.SpecificAge, -1))

      cachePolicyManager.cachePolicyHeadersFor(method) must throwAn[IllegalArgumentException]
    }
  }
}


