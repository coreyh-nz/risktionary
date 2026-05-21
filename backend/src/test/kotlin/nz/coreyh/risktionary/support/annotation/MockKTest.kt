package nz.coreyh.risktionary.support.annotation

import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.extension.ExtendWith
import java.lang.annotation.Inherited

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Inherited
@ExtendWith(MockKExtension::class)
@MockKExtension.CheckUnnecessaryStub
annotation class MockKTest
