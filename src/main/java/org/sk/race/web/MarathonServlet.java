package org.sk.race.web;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.sk.race.entities.Gender;
import org.sk.race.entities.Race;
import org.sk.race.entities.RaceItem;
import org.sk.race.entities.Runner;
import org.sk.race.reader.TxtReader;

import java.io.IOException;
import java.util.logging.Logger;

public class MarathonServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(MarathonServlet.class.getName());

    private Race race;

    public void init() {
        race = TxtReader.readRace();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        JSONObject res = new JSONObject();

        String strId = req.getParameter("id");
        RaceItem runnerInfo = findRaceItem(strId);

        if (runnerInfo != null) {
            res.put("status", "success");
            JSONObject jsonRunner = toJson(runnerInfo);
            res.put("runner", jsonRunner);
        } else {
            printErrorStatus(res, "Runner not found");
            logger.info(String.format("Runner %s not found", strId));
        }

        writeResponse(res, resp);
    }

    private void printStatus(JSONObject jsonRes, String status, String statusMessage) {
        jsonRes.put("status", status);
        jsonRes.put("message", statusMessage);
    }

    private void printErrorStatus(JSONObject jsonRes, String errorMessage) {
        printStatus(jsonRes, "error", errorMessage);
    }

    private void printSuccessStatus(JSONObject jsonRes, String successMessage) {
        printStatus(jsonRes, "success", successMessage);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        String strId = req.getParameter("id");
        RaceItem raceItem = buildRaceItem(req);
        RaceItem realRaceItem = findRaceItem(strId);
        if (realRaceItem != null) {
            updateFields(raceItem, realRaceItem);
        } else {
            int id = -1;
            if (StringUtils.isNotBlank(strId)) {
                logger.info(String.format("RaceItem with id %s not found, create a new one with this id", strId));
                id = Integer.parseInt(strId);
            } else {
                logger.info(String.format("No id was passed to the GET, create a new RaceItem with id %s", strId));
                id = findMaxId() + 1;
            }
            raceItem.setId(id);
            race.addResult(raceItem);
        }

        JSONObject res = new JSONObject();
        printSuccessStatus(res, "Runner added successfully");
        res.put("id", strId);
        writeResponse(res, resp);
    }

    private int findMaxId() {
        int res = -1;
        for (RaceItem ri : race.getResults()) {
            if (ri.getId() > res) {
                res = ri.getId();
            }
        }
        return res;
    }

    private void updateFields(RaceItem source, RaceItem target) {
        target.setTime(source.getTime());
        target.getRunner().setAge(source.getRunner().getAge());
        target.getRunner().setCountry(source.getRunner().getCountry());
        target.getRunner().setGender(source.getRunner().getGender());
        target.getRunner().setName(source.getRunner().getName());
    }


    private RaceItem buildRaceItem(HttpServletRequest req) {
        String name = req.getParameter("name");
        String time = req.getParameter("time");
        String age = req.getParameter("age");
        String country = req.getParameter("country");
        String gender = req.getParameter("gender");

        Runner runner = new Runner(name, Integer.parseInt(age), country, Gender.fromString(gender));
        RaceItem res = new RaceItem(-1, runner, RaceItem.parseTimeToSeconds(time));

        return res;
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) {
        String strId = req.getParameter("id");
        RaceItem raceItem = buildRaceItem(req);
        RaceItem realRaceItem = findRaceItem(strId);
        if (realRaceItem != null) {
            updateFields(raceItem, realRaceItem);
            JSONObject res = new JSONObject();
            printSuccessStatus(res, "Runner updated successfully");
            res.put("id", strId);
            writeResponse(res, resp);
        } else {
            JSONObject res = new JSONObject();
            printErrorStatus(res, "Runner not found");
            writeResponse(res, resp);
        }
    }

    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        JSONObject res = new JSONObject();
        String strId = req.getParameter("id");

        RaceItem raceItem = findRaceItem(strId);
        if (raceItem != null) {
            int id = Integer.parseInt(strId);
            int indexToRemove = -1;
            for (int i = 0; i < race.getResults().size(); i++) {
                if (race.getResults().get(i).getId() == id) {
                    indexToRemove = i;
                }
            }
            race.getResults().remove(indexToRemove);

            if (indexToRemove > -1) {
                printSuccessStatus(res, "deleted successfully");
            } else {
                printErrorStatus(res, "Runner not found");
            }
        } else {
            if (StringUtils.isBlank(strId)) {
                printErrorStatus(res, "Runner id not found");
            } else {
                printErrorStatus(res, "Runner not found");
            }
        }
        writeResponse(res, resp);
    }

    private void writeResponse(JSONObject res, HttpServletResponse resp) {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            resp.getWriter().print(res);
        } catch (IOException e) {
            logger.severe(" response can not be written");
        }
    }

    private RaceItem findRaceItem(String strId) {
        RaceItem res = null;

        if (race != null) {
            if (!StringUtils.isBlank(strId)) {
                Integer id = -1;
                try {
                    id = Integer.parseInt(strId);
                } catch (NumberFormatException e) {
                    logger.fine("ID is not valid ");

                }
                if (id > -1) {
                    for (RaceItem item : race.getResults()) {
                        if (item.getId() == id) {
                            res = item;
                        }
                    }
                }
            } else {
                logger.fine("ID parameter is empty or null" + strId);

            }
        } else {
            logger.severe("Race object was not initialized");
        }
        return res;
    }

    private JSONObject toJson(RaceItem runnerInfo) {
        JSONObject jsonRunner = new JSONObject();
        jsonRunner.put("id", runnerInfo.getId());
        jsonRunner.put("name", runnerInfo.getRunner().getName());
        jsonRunner.put("time", runnerInfo.getStringTime());
        jsonRunner.put("gender", runnerInfo.getRunner().getGender());
        jsonRunner.put("age", runnerInfo.getRunner().getAge());
        jsonRunner.put("country", runnerInfo.getRunner().getCountry());
        return jsonRunner;
    }

}
