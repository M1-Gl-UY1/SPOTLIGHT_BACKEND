const sequelize = require("../sequelize");

const { models: { User, Provider } } = require("../models");

const { Op } = require("sequelize");

const formatError = (err) => {
  if (err.errors && err.errors.length > 0) {
    return err.errors.map(e => e.message);
  }
  return err.message;
};

const create = async (req, res) => {
  try {
    const { userId, name, phone, email, certifications, nationalId, degrees, rating } = req.body;
    console.log(req.body);

    const user = await User.findByPk(userId);
    if (!user) return res.status(404).json({ message: "User not found" });

    /*const existing = await Provider.findOne({ where: { userId } });
    if (existing) {
      return res.status(400).json({ message: "User already has a provider profile" });
    }*/

    const provider = await Provider.create({
      userId,
      name,
      phone,
      email,
      certifications,
      nationalId,
      degrees,
      rating
    });

    return res.status(201).json(provider);
  } catch (err) {
    return res.status(400).json({ error: formatError(err) });
  }
};

const getById = async (req, res) => {
  console.log("get provider by id");
  try {
    const { id } = req.params;

    const provider = await Provider.findByPk(id, {
      include: [{ model: User, attributes: ["id", "firstName", "lastName", "email"] }]
    });

    if (!provider) return res.status(404).json({ message: "Provider not found" });

    return res.status(200).json(provider);
  } catch (err) {
    return res.status(400).json({ error: formatError(err) });
  }
};

const getAll = async (req, res) => {
  try {
    const { page = 1, limit = 10, search = "" } = req.query;
    const offset = (page - 1) * limit;

    const where = search
      ? { name: { [Op.iLike]: `%${search}%` } }
      : {};

    const providers = await Provider.findAndCountAll({
      where,
      limit: parseInt(limit),
      offset,
      order: [["createdAt", "DESC"]]
    });

    return res.status(200).json({
      total: providers.count,
      page: parseInt(page),
      pages: Math.ceil(providers.count / limit),
      data: providers.rows
    });
  } catch (err) {
    return res.status(400).json({ error: formatError(err) });
  }
};

const update = async (req, res) => {
  try {
    const { id } = req.params;
    const { name, email, phone, certifications, nationalId, degrees, rating } = req.body;

    const provider = await Provider.findByPk(id);
    if (!provider) return res.status(404).json({ message: "Provider not found" });

    await provider.update({
      name: name !== undefined ? name : provider.name,
      email: email !== undefined ? email : provider.email,
      phone: phone !== undefined ? phone : provider.phone,
      certifications: certifications !== undefined ? certifications : provider.certifications,
      nationalId: nationalId !== undefined ? nationalId : provider.nationalId,
      degrees: degrees !== undefined ? degrees : provider.degrees,
      rating: rating !== undefined ? rating : provider.rating
    });

    await provider.reload();

    return res.status(200).json(provider);
  } catch (err) {
    return res.status(400).json({ error: formatError(err) });
  }
};

const remove = async (req, res) => {
  try {
    const { id } = req.params;

    const provider = await Provider.findByPk(id);
    if (!provider) return res.status(404).json({ message: "Provider not found" });

    await provider.destroy();

    return res.status(200).json({ message: "Provider deleted successfully" });
  } catch (err) {
    return res.status(400).json({ error: formatError(err) });
  }
};

const updateRating = async (req, res) => {
  try {
    const { id } = req.params;
    const { rating } = req.body;

    if (rating < 0 || rating > 5) {
      return res.status(400).json({ message: "Rating must be between 0 and 5" });
    }

    const provider = await Provider.findByPk(id);
    if (!provider) return res.status(404).json({ message: "Provider not found" });

    provider.rating = rating;
    await provider.save();

    return res.status(200).json(provider);
  } catch (err) {
    return res.status(400).json({ error: formatError(err) });
  }
};

module.exports = { create, getById, getAll, update, remove, updateRating };
