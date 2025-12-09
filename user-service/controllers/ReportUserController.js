const sequelize = require("../sequelize");

const User = require("../models/User")(sequelize);
const ReportUser = require("../models/ReportUser")(sequelize);

ReportUser.associate({ User });

const { Op } = require("sequelize");

const formatError = (err) => {
  if (err.errors && err.errors.length > 0) {
    return err.errors.map(e => e.message);
  }
  return err.message;
};

const create = async (req, res) => {
  try {
    const { reporterId, reportedUserId, reason, severity } = req.body;
    console.log(req.body);

    const reporter = await User.findByPk(reporterId);
    if (!reporter) return res.status(404).json({ message: "Reporter not found" });

    const reported = await User.findByPk(reportedUserId);
    if (!reported) return res.status(404).json({ message: "Reported user not found" });

    const report = await ReportUser.create({
      reporterId,
      reportedUserId,
      reason,
      severity
    });

    return res.status(201).json(report);
  } catch (err) {
    return res.status(400).json({ error: formatError(err) });
  }
};

const getAll = async (req, res) => {
  try {
    const { page = 1, limit = 10, status = "", severity = "", search = "" } = req.query;
    const offset = (page - 1) * limit;

    const where = {};

    if (status) where.status = status;
    if (severity) where.severity = severity;
    if (search) where.reason = { [Op.iLike]: `%${search}%` };

    const reports = await ReportUser.findAndCountAll({
      where,
      limit: parseInt(limit),
      offset,
      order: [["createdAt", "DESC"]],
      include: [
        { model: User, as: "reporter", attributes: ["id", "firstName", "lastName", "email"] },
        { model: User, as: "reportedUser", attributes: ["id", "firstName", "lastName", "email"] }
      ]
    });

    return res.status(200).json({
      total: reports.count,
      page: parseInt(page),
      pages: Math.ceil(reports.count / limit),
      data: reports.rows
    });
  } catch (err) {
    return res.status(400).json({ error: formatError(err) });
  }
};

const getById = async (req, res) => {
  try {
    const { id } = req.params;

    const report = await ReportUser.findByPk(id, {
      include: [
        { model: User, as: "reporter", attributes: ["id", "firstName", "lastName", "email"] },
        { model: User, as: "reportedUser", attributes: ["id", "firstName", "lastName", "email"] }
      ]
    });

    if (!report) return res.status(404).json({ message: "Report not found" });

    return res.status(200).json(report);
  } catch (err) {
    return res.status(400).json({ error: formatError(err) });
  }
};

const update = async (req, res) => {
  try {
    const { id } = req.params;
    const { reason, severity, status } = req.body;

    const report = await ReportUser.findByPk(id);
    if (!report) {
      return res.status(404).json({ message: "Report not found" });
    }

    const updateFields = {};

    if (reason !== undefined) {
      updateFields.reason = reason;
    }

    if (severity !== undefined) {
      const allowedSeverity = ["low", "medium", "high", "critical"];
      if (!allowedSeverity.includes(severity)) {
        return res.status(400).json({ message: "Invalid severity value" });
      }
      updateFields.severity = severity;
    }

    if (status !== undefined) {
      const allowedStatus = ["pending", "reviewed", "resolved", "dismissed"];
      if (!allowedStatus.includes(status)) {
        return res.status(400).json({ message: "Invalid status value" });
      }
      updateFields.status = status;
    }

    await report.update(updateFields);

    return res.status(200).json({
      message: "Report updated successfully",
      report
    });

  } catch (err) {
    return res.status(400).json({ error: formatError(err) });
  }
};


const updateStatus = async (req, res) => {
  try {
    const { id } = req.params;
    const { status } = req.body;
    console.log(status);
    
    const validStatus = ["pending", "reviewed", "resolved", "dismissed"];
    if (!validStatus.includes(status)) {
      return res.status(400).json({ message: "Invalid status value" });
    }

    const report = await ReportUser.findByPk(id);
    if (!report) return res.status(404).json({ message: "Report not found" });

    report.status = status;
    await report.save();

    return res.status(200).json(report);
  } catch (err) {
    return res.status(400).json({ error: formatError(err) });
  }
};

const remove = async (req, res) => {
  try {
    const { id } = req.params;

    const report = await ReportUser.findByPk(id);
    if (!report) return res.status(404).json({ message: "Report not found" });

    await report.destroy();

    return res.status(200).json({ message: "Report deleted successfully" });
  } catch (err) {
    return res.status(400).json({ error: formatError(err) });
  }
};

module.exports = { create, getAll, getById, update, updateStatus, remove };
