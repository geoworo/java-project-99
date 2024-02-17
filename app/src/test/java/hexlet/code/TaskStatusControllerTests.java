package hexlet.code;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.status.TaskStatusUpdateDTO;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskStatusControllerTests {
    @Autowired
    private MockMvc mm;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TaskStatusMapper tsm;

    @Autowired
    private TaskStatusRepository tsr;

    @Autowired
    private UserRepository ur;

    private TaskStatus ts;
    private User user;
    private JwtRequestPostProcessor token;

    @BeforeEach
    public void setUp() {
        ts = Generator.generateStatus();
        user = Generator.generateUser();
        ur.save(user);
        token = jwt().jwt(builder -> builder.subject(user.getEmail()));
    }

    @AfterEach
    public void clean() {
        tsr.deleteAll();
        ur.deleteAll();
    }

    @Test
    public void testIndex() throws Exception {
        tsr.save(ts);
        var result = mm.perform(get("/api/task_statuses").with(token))
                .andExpect(status().isOk()).andReturn();
        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
    }

    @Test
    public void testShow() throws Exception {
        tsr.save(ts);
        var result = mm.perform(get("/api/task_statuses/" + ts.getId()).with(token))
                .andExpect(status().isOk())
                .andReturn();
        var body =  result.getResponse().getContentAsString();
        assertThatJson(body).and(
                v -> v.node("name").isEqualTo(ts.getName()),
                v -> v.node("slug").isEqualTo(ts.getSlug())
        );
    }

    @Test
    public void testCreate() throws Exception {
        var dto = tsm.map(ts);
        var request = post("/api/task_statuses")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));
        mm.perform(request)
                .andExpect(status().isCreated());
        var tstatus = tsr.findBySlug(dto.getSlug()).get();
        assertNotNull(tstatus);
        assertThat(tstatus.getName()).isEqualTo(dto.getName());
    }

    @Test
    public void testUpdate() throws Exception {
        tsr.save(ts);
        var dto = new TaskStatusUpdateDTO();
        dto.setName(JsonNullable.of("name"));
        var request = put("/api/task_statuses/" + ts.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));
        mm.perform(request)
                .andExpect(status().isOk());
        var tstatus = tsr.findById(ts.getId()).get();
        assertThat(tstatus.getName()).isEqualTo(dto.getName().get());
    }

    @Test
    public void testDelete() throws Exception {
        tsr.save(ts);
        mm.perform(delete("/api/task_statuses/" + ts.getId()).with(token))
                .andExpect(status().isNoContent());
        assertThat(tsr.existsById(ts.getId())).isEqualTo(false);
    }
}
