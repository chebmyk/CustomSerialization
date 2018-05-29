import com.mein.data.Child;
import com.mein.data.Parent;
import com.mein.json.JsonSerialization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonSerializationTest {

    Parent parent1 = new Parent(30,"ParentName1");
    Parent parent2 = new Parent(35,"ParentName2");
    Child child = new Child(8,"ChildName");

    @BeforeEach
    void setUp() {
            parent1.addChild(child);
            parent2.addChild(child);
    }



    @Test
    void readJsonObjectEquals() {
       Parent p2 = (Parent)JsonSerialization.readJsonObject(parent1.getClass());
       assertEquals(p2.getAge(),parent1.getAge());
       assertEquals(p2.getName(),parent1.getName());
       assertArrayEquals(p2.getChildren().toArray(),parent1.getChildren().toArray());
    }
}