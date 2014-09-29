import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
/**
 * Component implementing the Hello service.
 * This class used annotations to describe the component type.
 * @author <a href="mailto:dev@felix.apache.org">Felix Project Team</a>
 */
@Component
@Provides
@Instantiate // Declare an instance of the component
public class HelloImpl implements Hello {
    @ServiceProperty
    public String boo = "boo";
    @ServiceProperty
    public String bla = "bla";
    /**
     * Returns an 'Hello' message.
     * @param name : name
     * @return Hello message
     * @see ipojo.example.hello.Hello#sayHello(String)
     */
    public String sayHello(String name) { return "hello " + name + "."; }
}