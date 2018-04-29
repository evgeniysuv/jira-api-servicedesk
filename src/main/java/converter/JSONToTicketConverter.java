package converter;

import javafx.util.Pair;
import model.Ticket;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Created by esuv on 4/19/18
 */
@Component
public class JSONToTicketConverter implements Converter<Pair<String, JSONObject>, Ticket> {

    @NotNull
    @Override
    public Ticket convert(@NotNull Pair<String, JSONObject> issue) {

        return null;
    }
}
